/*
 * Project: Conductor
 * Copyright (C) 2019 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alflabs.conductor.v2;

import com.alflabs.annotations.Null;
import com.alflabs.conductor.IEntryPoint;
import com.alflabs.conductor.jmri.IJmriProvider;
import com.alflabs.conductor.jmri.IJmriSensor;
import com.alflabs.conductor.util.Analytics;
import com.alflabs.conductor.util.EventLogger;
import com.alflabs.conductor.util.JsonSender;
import com.alflabs.conductor.util.LogException;
import com.alflabs.conductor.util.MqttClient;
import com.alflabs.conductor.util.Pair;
import com.alflabs.conductor.v2.ui.IStatusWindow;
import com.alflabs.conductor.v2.ui.IWindowCallback;
import com.alflabs.conductor.v2.ui.StatusWindow2k;
import com.alflabs.kv.KeyValueServer;
import com.alflabs.utils.IClock;
import com.alflabs.utils.ILogger;
import com.alfray.conductor.v2.simulator.ISimulUiCallback;
import com.alfray.conductor.v2.simulator.dagger.DaggerISimul2kComponent;
import com.alfray.conductor.v2.simulator.dagger.ISimul2kComponent;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import javax.inject.Inject;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class EntryPoint2 implements IEntryPoint, IWindowCallback {
    private static final String TAG = EntryPoint2.class.getSimpleName();

    private boolean mIsSimulation;
    private IStatusWindow mWin;
    private Thread mHandleThread;
    private Thread mWinUpdateThread;
    private final ZeroConfController mZeroConf = new ZeroConfController();
    private final KVServerController mKVController = new KVServerController();
    private final StringBuilder mStatus = new StringBuilder();
    private final StringBuilder mLoadError = new StringBuilder();
    private final AtomicBoolean mKeepRunning = new AtomicBoolean(true);
    private final AtomicBoolean mPaused = new AtomicBoolean();
    private Optional<IEngineAdapter> mAdapter = Optional.empty();
    private Optional<ISimul2kComponent> mSimul2kComponent = Optional.empty();

    @Inject IClock mClock;
    @Inject ILogger mLogger;
    @Inject Analytics mAnalytics;
    @Inject MqttClient mMqttClient;
    @Inject JsonSender mJsonSender;
    @Inject EventLogger mEventLogger;
    @Inject KeyValueServer mKeyValueServer;

    /**
     * Entry point invoked from DevEntryPoint2.
     */
    public void init(@Null String simulationScript) {
        mIsSimulation = true;

        mSimul2kComponent = Optional.of(DaggerISimul2kComponent.factory().createComponent());
        setup(mSimul2kComponent.get().getJmriProvider(), simulationScript);
    }

    /**
     * Entry point invoked directly from JMRI Jython Conductor.py.
     */
    @Override
    public boolean setup(IJmriProvider jmriProvider, String scriptPath) {
        log("Setup");

        File scriptFile = new File(scriptPath);
        boolean scriptExists = scriptFile.exists();
        String mode = guessEngineMode(scriptFile);

        if ("legacy".equals(mode)) {
            throw new IllegalArgumentException("Conductor1 script is not supported.");

        } else if ("groovy".equals(mode)) {
            throw new IllegalArgumentException("Groovy Conductor2 script is not supported.");

        } else if ("kts".equals(mode)) {
            Engine2KotlinAdapter adapter = new Engine2KotlinAdapter();
            mAdapter = Optional.of(adapter);
            Engine2KotlinAdapter.LocalComponent2k component = DaggerEngine2KotlinAdapter_LocalComponent2k
                    .factory()
                    .createComponent(jmriProvider);
            // Do not use any injected fields before this call
            component.inject(this);
            component.inject(adapter);
            adapter.setSimulator(mSimul2kComponent.orElse(null));

        } else {
            log("Unknown engine mode " + mode);
            return false;
        }
        log("Engine mode: " + mode);
        log("Script: " + scriptPath + " " + (scriptExists ? "exists" : "DOES NOT EXIST"));

        mAdapter.get().setScriptFile(scriptFile);

        String eventLogFilename = mEventLogger.start(null);
        log("Event log: " + eventLogFilename);

        mKVController.start(mLogger, mKeyValueServer);
        mZeroConf.start(mLogger, scriptFile);
        openWindow();
        onWindowReload();

        return true;
    }

    private String guessEngineMode(File scriptFile) {
        String mode = "legacy";
        try {
            if (scriptFile.getName().endsWith(".groovy")) {
                mode = "groovy";
            } else if (scriptFile.getName().endsWith(".kts")) {
                mode = "kts";
            } else {
                String source = Files.toString(scriptFile, Charsets.UTF_8);
                if (source.contains("import groovy")
                        || source.contains("import com.alflabs.conductor.v2.script.RootScript")
                        || source.contains("\ndef ")) {
                    mode = "groovy";
                } else if (source.contains("\nval ") || source.contains("throttle(")) {
                    mode = "kts";
                }
            }

            return mode;
        } catch (IOException e) {
            log("Failed to read source from " + scriptFile);
            return null;
        }
    }

    /**
     * Invoked from JMRI Jython Conductor.py as a loop or from the simulated thread
     * or from _simulHandleThread in the dev environment.
     */
    @Override
    public boolean handle() {
        if (!mKeepRunning.get()) {
            log("Stop Requested");
            return false;
        }

        mAdapter.ifPresent(adapter -> adapter.onHandle(mPaused));
        return true;
    }

    public void runDevLoop() {
        if (mHandleThread == null) {
            mHandleThread = new Thread(this::_simulHandleThread, "EntryPoint2-HandleThread");
            mHandleThread.start();
        }
    }

    private void _simulHandleThread() {
        log("Simul Handle Thread - Start");
        while (mKeepRunning.get()) {
            try {
                handle();
            } catch (Exception e) {
                log("Simul Handle Thread - Exception (ignored): "
                        + _getStackTrace(e));
            }
        }
        log("Simul Handle Thread - End");
    }

    private void _windowUpdateThread() {
        log("Window Update Thread - Start");

        while (mKeepRunning.get()) {
            IStatusWindow win = mWin;
            if (win != null) {
                try {
                    updateWindowLog();
                    win.updateUI();
                    Thread.sleep(330 /*ms*/);
                } catch (InterruptedException ignore) {
                }
            }
        }

        log("Window Update Thread - End");
    }

    private void openWindow() {
        // Note: it's fine for opening the window to fail if this runs from a terminal or as a service.
        try {
            if (GraphicsEnvironment.isHeadless()) {
                log("StatusWindow2 skipped: headless graphics environment");
            } else {
                mWin = new StatusWindow2k();
                mWin.open(this);
                mWin.updateScriptName("No Script1 Loaded");

                if (mWinUpdateThread == null) {
                    mWinUpdateThread = new Thread(this::_windowUpdateThread, "EntryPoint2-WinUpdate");
                    mWinUpdateThread.start();
                }
            }
        } catch (Exception e) {
            log("StatusWindow2 Failed: " + _getStackTrace(e));
            mWin = null;
        }
    }

    @Override
    public void onQuit() {
        log("onQuit");
        mJsonSender.sendEvent("conductor", null, "off");
        sendEvent("Stop");
        mZeroConf.stop();
        mKVController.stop();

        mKeepRunning.set(false);

        if (mHandleThread != null) {
            try {
                mHandleThread.join();
                log("Simul Handle Thread terminated");
            } catch (InterruptedException e) {
                log("Simul Handle Thread join: " + e);
            } finally {
                mHandleThread = null;
            }
        }

        try {
            mJsonSender.shutdown();
        } catch (InterruptedException e) {
            mLogger.d(TAG, "Teardown JsonSender exception: " + e);
        }

        try {
            mAnalytics.shutdown();
        } catch (Exception e) {
            mLogger.d(TAG, "Teardown Analytics exception: " + e);
        }

        try {
            mMqttClient.shutdown();
        } catch (Exception e) {
            mLogger.d(TAG, "Teardown MqttClient exception: " + e);
        }

        if (mWinUpdateThread != null) {
            // Sometimes the win update threads is still stuck on the Swing invokeAndWait;
            // use invokeLater for most UI updates instead.
            try {
                mWinUpdateThread.join();
                log("Window Update Thread terminated");
            } catch (InterruptedException e) {
                log("Window Update Thread join: " + e);
            } finally {
                mWinUpdateThread = null;
            }
        }

        mWin = null;
        if (mIsSimulation) {
            log("Exit Simulation");
            System.exit(0);
        }
    }

    @Override
    public void onWindowReload() {
        log("onWindowReload");

        mPaused.set(true);
        mWin.clearUpdates();
        mLoadError.setLength(0);

        if (!mAdapter.isPresent()) {
            log("onWindowReload: no engine adapter.");
            return;
        }

        try {
            // Creates the daggers script component, loads the script, executes onExecStart.
            Pair<Boolean, File> reloaded = mAdapter.get().onReload();
            boolean wasRunning = reloaded.mFirst;
            File file = reloaded.mSecond;

            IStatusWindow win = mWin;
            if (win != null) {
                win.updateScriptName(file.getName());
                loadMap();
            }

            if (wasRunning) {
                sendEvent("Reload");
            } else {
                sendEvent("Start");
                mJsonSender.sendEvent("conductor", null, "on");
            }
        } catch (Exception e) {
            log("Failed to load event script with the following exception:");
            LogException.logException(mLogger, TAG, e);

            mLoadError.append(e).append('\n').append(_getStackTrace(e));
            if (mWin == null) {
                log("Parsing Exception: " + _getStackTrace(e));
            }
        }

        registerUiThrottles();
        registerUiConditionals();

        mPaused.set(false);
    }

    private void loadMap() {
        mAdapter.ifPresent(adapter ->
            adapter.getScriptFile().ifPresent(scriptFile -> {
                adapter.getLoadedMapName().ifPresent(mapInfo -> {
                    String svgName = mapInfo.getName();
                    try {
                        URI svgUri = mapInfo.toURI();
                        log("Loading map '" + svgName + "' from : " + svgUri);
                        mWin.displaySvgMap(mapInfo.getSvg(), svgUri);
                    } catch (Exception e) {
                        log("Failed to load map '" + svgName + "' : " + e);
                    }
                });
            })
        );
    }

    @Override
    public void onWindowPause() {
        log("onWindowPause");
        mPaused.set(!mPaused.get());
        IStatusWindow win = mWin;
        if (win != null) {
            win.updatePause(mPaused.get());
        }
    }

    @Override
    public void onWindowSvgLoaded() {
        log("onWindowSvgLoaded");
        IStatusWindow win = mWin;
        if (win == null) return;
        win.setSimulationMode(mIsSimulation);
        if (!mIsSimulation) {
            win.enterKioskMode();
        } else {
            log("Simulation: Skip Kiosk Mode.");
        }

    }

    @Override
    public void onWindowSvgClick(String itemId) {
        if (!mIsSimulation) {
            mLogger.d(TAG, "SVG click on #" + itemId + " ignored when not in simulation.");
            return;
        }

        final AtomicBoolean processed = new AtomicBoolean();
        mSimul2kComponent.ifPresent(comp -> {
            IJmriProvider jmri = comp.getJmriProvider();
            if (itemId.startsWith("S-")) {
                String blockId = itemId.substring(2);
                IJmriSensor sensor = jmri.getSensor(blockId);
                if (sensor != null) {
                    mLogger.d(TAG, "SVG click on block " + itemId);
                    sensor.setActive(!sensor.isActive());
                    processed.set(true);
                }
            }
        });

        if (!processed.get()) {
            mLogger.d(TAG, "SVG click on unknown element #" + itemId + " ignored.");
        }
    }

    @Override
    public void onFlaky(boolean isFlaky) {
        mSimul2kComponent.ifPresent(comp -> {
            IJmriProvider jmri = comp.getJmriProvider();
            if (jmri instanceof ISimulUiCallback) { // TODO make a better interface composition
                ((ISimulUiCallback) jmri).setFlaky(isFlaky);
            }
        });
    }

    /** Executes on the Swing EDT thread. */
    private void updateWindowLog() {
        IStatusWindow win = mWin;
        if (win == null) return;

        mStatus.setLength(0);

        if (mLoadError.length() > 0) {
            mStatus.append("\n--- [ LOAD ERROR ] ---\n");
            mStatus.append(mLoadError);
        }

        mAdapter.ifPresent(adapter -> adapter.appendToLog(mStatus));
        appendVarStatus(mStatus, mKeyValueServer);

        win.updateMainLog(mStatus.toString());

        if (mIsSimulation) {
            mSimul2kComponent.ifPresent( comp ->
                    win.updateSimuLog(comp.getSimul2k().getUiLogOutput()));
        } else {
            win.updateSimuLog("Simulator not running.");
        }
    }

    private static void appendVarStatus(
            StringBuilder outStatus,
            KeyValueServer kvServer) {

        outStatus.append("--- [ KV Server ] ---\n");
        outStatus.append("Connections: ").append(kvServer.getNumConnections()).append('\n');
        for (String key : kvServer.getKeys()) {
            String value = kvServer.getValue(key);
            if (value.contains("[{") && value.length() > 50) {
                // Shorten long JSON data
                value = value.substring(0, 50) + "...}";
            }
            outStatus
                    .append('[').append(key).append("] = ")
                    .append(value)
                    .append('\n');
        }
    }

    private void log(String line) {
        if (mLogger == null) {
            System.out.println(TAG + " " + line);
        } else {
            mLogger.d(TAG, line);
        }
    }

    private void sendEvent(String action) {
        mAnalytics.sendEvent("Conductor", action, "", "Conductor");
    }

    private void registerUiThrottles() {
        IStatusWindow win = mWin;
        if (win == null) { return; }

        mAdapter.ifPresent(adapter ->
                win.registerThrottles(adapter.getThrottles()));
    }

    private void registerUiConditionals() {
        IStatusWindow win = mWin;
        if (win == null) { return; }

        mAdapter.ifPresent(adapter ->
                win.registerActivables(
                    adapter.getSensors(),
                    adapter.getBlocks(),
                    adapter.getTurnouts()
        ));
    }

    private static String _getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw, true));
        return sw.toString();
    }
}
