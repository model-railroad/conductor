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

import autovalue.shaded.org.apache.commons.lang.exception.ExceptionUtils;
import com.alflabs.annotations.Null;
import com.alflabs.conductor.IEntryPoint;
import com.alflabs.conductor.dagger.CommonModule;
import com.alflabs.conductor.jmri.FakeJmriProvider;
import com.alflabs.conductor.jmri.IJmriProvider;
import com.alflabs.conductor.util.Analytics;
import com.alflabs.conductor.util.EventLogger;
import com.alflabs.conductor.util.JsonSender;
import com.alflabs.conductor.util.LogException;
import com.alflabs.conductor.v1.Script1Context;
import com.alflabs.conductor.v1.Script1Loader;
import com.alflabs.conductor.v1.dagger.IEngine1Component;
import com.alflabs.conductor.v1.dagger.IScript1Component;
import com.alflabs.conductor.v1.script.Enum_;
import com.alflabs.conductor.v1.script.ExecEngine1;
import com.alflabs.conductor.v1.script.Script1;
import com.alflabs.conductor.v1.script.Sensor;
import com.alflabs.conductor.v1.script.Timer;
import com.alflabs.conductor.v1.script.Turnout;
import com.alflabs.conductor.v1.script.Var;
import com.alflabs.conductor.v2.ui.IWindowCallback;
import com.alflabs.conductor.v2.ui.StatusWindow2;
import com.alflabs.kv.KeyValueServer;
import com.alflabs.manifest.MapInfo;
import com.alflabs.utils.IClock;
import com.alflabs.utils.ILogger;
import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ConcurrentModificationException;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class EntryPoint2 implements IEntryPoint, IWindowCallback {
    private static final String TAG = EntryPoint2.class.getSimpleName();

    private boolean mIsSimulation;
    private LocalComponent1 mComponent;
    private StatusWindow2 mWin;
    private Thread mHandleThread;
    private Thread mWinUpdateThread;
    private final StringBuilder mStatus = new StringBuilder();
    private final StringBuilder mLoadError = new StringBuilder();
    private final AtomicBoolean mKeepRunning = new AtomicBoolean(true);
    private final AtomicBoolean mPaused = new AtomicBoolean();

    @Inject ILogger mLogger;
    @Inject IClock mClock;
    @Inject KeyValueServer mKeyValueServer;
    @Inject EventLogger mEventLogger;
    @Inject Analytics mAnalytics;
    @Inject JsonSender mJsonSender;
    @Inject Script1Loader mScript1Loader;
    @Inject Script1Context mScript1Context;

    /**
     * Entry point invoked from DevEntryPoint2.
     */
    public void init(@Null String simulationScript) {
        mIsSimulation = true;
        FakeJmriProvider jmriProvider = new FakeJmriProvider();
        setup(jmriProvider, simulationScript);
    }

    /**
     * Entry point invoked directly from JMRI Jython Conductor.py.
     */
    @Override
    public boolean setup(IJmriProvider jmriProvider, String scriptPath) {
        File scriptFile = new File(scriptPath);
        mComponent = DaggerEntryPoint2_LocalComponent1
                .factory()
                .createComponent(jmriProvider);

        // Do not use any injected field before this call
        mComponent.inject(this);
        mScript1Context.setScript1File(scriptFile);

        logln("Setup");

        String eventLogFilename = mEventLogger.start(null);
        logln("Event log: " + eventLogFilename);

        openWindow();
        onWindowReload();

        return true;
    }

    /**
     * Invoked from JMRI Jython Conductor.py as a loop or from the simulated thread
     * or from _simulHandleThread in the dev environment.
     */
    @Override
    public boolean handle() {
        if (!mKeepRunning.get()) {
            logln("Stop Requested");
            return false;
        }

        Optional<ExecEngine1> engine = mScript1Context.getExecEngine1();
        // If we have no engine, or it is paused, just idle-wait.
        if (!engine.isPresent() || mPaused.get()) {
            // TODO poor man async handling.
            // Consider some kind of CountDownLatch or a long monitor/notify or similar
            // instead of an active wait.
            try {
                Thread.sleep(330 /*ms*/);
            } catch (InterruptedException ignore) {
            }
            return true;
        }

        engine.get().onExecHandle();
        return true;
    }

    public void runDevLoop() {
        if (mHandleThread == null) {
            mHandleThread = new Thread(this::_simulHandleThread, "EntryPoint2-HandleThread");
            mHandleThread.start();
        }
    }

    private void _simulHandleThread() {
        logln("Simul Handle Thread - Start");
        while (mKeepRunning.get()) {
            try {
                handle();
            } catch (Exception e) {
                logln("Simul Handle Thread - Exception (ignored): "
                        + ExceptionUtils.getStackTrace(e));
            }
        }
        logln("Simul Handle Thread - End");
    }

    private void _windowUpdateThread() {
        logln("Window Update Thread - Start");

        while (mKeepRunning.get() && mWin != null) {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    updateWindowLog();

//                    try {
//                        mUiUpdaters.values().forEach(r -> {
//                            try {
//                                r.run();
//                            } catch (Exception e) {
//                                logln("Window Update Thread - Exception (ignored): "
//                                        + ExceptionUtils.getStackTrace(e));
//                            }
//                        });
//                    } catch (ConcurrentModificationException ignore) {
//                    } catch (Exception e2) {
//                        logln("Window Update Thread - Exception (ignored): "
//                                + ExceptionUtils.getStackTrace(e2));
//                    }
                });

                Thread.sleep(330 /*ms*/);
            } catch (InterruptedException | InvocationTargetException ignore) {
            }
        }

        logln("Window Update Thread - End");
    }

    private void openWindow() {
        // Note: it's fine for opening the window to fail if this runs from a terminal or as a service.
        try {
            if (GraphicsEnvironment.isHeadless()) {
                logln("StatusWindow2 skipped: headless graphics environment");
            } else {
                mWin = new StatusWindow2();
                mWin.open(this);
                mWin.updateScriptName("No Script1 Loaded");

                if (mWinUpdateThread == null) {
                    mWinUpdateThread = new Thread(this::_windowUpdateThread, "EntryPoint2-WinUpdate");
                    mWinUpdateThread.start();
                }
            }
        } catch (Exception e) {
            logln("StatusWindow2 Failed: " + ExceptionUtils.getStackTrace(e));
            mWin = null;
        }
    }

    @Override
    public void onQuit() {
        logln("onQuit");
        mJsonSender.sendEvent("conductor", null, "off");
        sendEvent("Stop");

        mKeepRunning.set(false);

        if (mHandleThread != null) {
            try {
                mHandleThread.join();
                logln("Simul Handle Thread terminated");
            } catch (InterruptedException e) {
                logln("Simul Handle Thread join: " + e);
            } finally {
                mHandleThread = null;
            }
        }

        if (mWinUpdateThread != null) {
            // TODO sometimes the win update threads is still stuck on the Swing invokeAndWait.
            try {
                mWinUpdateThread.join();
                logln("Window Update Thread terminated");
            } catch (InterruptedException e) {
                logln("Window Update Thread join: " + e);
            } finally {
                mWinUpdateThread = null;
            }
        }

        mWin = null;
        if (mIsSimulation) {
            logln("Exit Simulation");
            System.exit(0);
        }
    }

    @Override
    public void onWindowReload() {
        logln("onWindowReload");

        boolean wasRunning = mScript1Context.getScript1Component().isPresent();
//        mUiUpdaters.clear();


//        } catch (IOException e) {
//        }
//        return error.toString();


        try {
            // TBD Release any resources from current script component as needed.
            mScript1Context.reset();

            File file = mScript1Context
                    .getScript1File()
                    .orElseThrow(() -> new IllegalArgumentException("Script1 File Not Defined"));
            logln("Script1 Path: " + file.getPath());
            mScript1Loader.execByPath(mScript1Context);

            if (mWin != null) {
                mWin.updateScriptName(file.getName());
                loadMap();
            }

            if (wasRunning) {
                sendEvent("Reload");
            } else {
                sendEvent("Start");
                mJsonSender.sendEvent("conductor", null, "on");
            }
        } catch (Exception e) {
            logln("Failed to load event script with the following exception:");
            LogException.logException(mLogger, TAG, e);

            mLoadError.setLength(0);
            mLoadError.append(e).append('\n').append(ExceptionUtils.getStackTrace(e));
            if (mWin == null) {
                logln("Parsing Exception: " + ExceptionUtils.getStackTrace(e));
            }
        }


        //
//        registerUiThrottles();
//        registerUiConditionals();
//

    }

    private void loadMap() {
        if (!mScript1Context.getScript1File().isPresent()) {
            return;
        }
        File scriptFile = mScript1Context.getScript1File().get();
        File scriptDir = scriptFile.getParentFile();

        Optional<Script1> script = mScript1Context.getScript1();
        if (script.isPresent()) {
            TreeMap<String, MapInfo> maps = script.get().getMaps();
            Optional<MapInfo> mapName = maps.values().stream().findFirst();
            if (mapName.isPresent()) {
                String svgName = mapName.get().getUri();
                File svgFile = scriptDir == null ? new File(svgName) : new File(scriptDir, svgName);
                URI svgUri = svgFile.toURI();

                logln("Loading map: " + svgUri);
                try {
                    mWin.displaySvgMap(svgUri);
                } catch (Exception e) {
                    logln("Failed to load map '" + svgName + "' : " + e);
                }
            }
        }
    }

    @Override
    public void onWindowPause() {
        logln("onWindowPause");
        mPaused.set(!mPaused.get());
    }

    @Override
    public void onWindowSvgLoaded() {

    }

    @Override
    public void onWindowSvgClick(String itemId) {

    }

    /** Executes on the Swing EDT thread. */
    private void updateWindowLog() {
        if (mWin == null) return;

        mStatus.setLength(0);

        if (mLoadError.length() > 0) {
            mStatus.append("\n--- [ LOAD ERROR ] ---\n");
            mStatus.append(mLoadError);
        }

        String lastError = mScript1Context.getError();
        if (lastError.length() > 0) {
            mStatus.append("\n--- [ LAST ERROR ] ---\n");
            mStatus.append(lastError);
        }

        Optional<ExecEngine1> engine = mScript1Context.getExecEngine1();
        Optional<Script1> script = mScript1Context.getScript1();
        if (engine.isPresent() && script.isPresent()) {
            try {
                appendVarStatus(mStatus, script.get(), engine.get(), mKeyValueServer);
            } catch (ConcurrentModificationException ignore) {}
        }

        mWin.updateLog(mStatus.toString());
    }

    private static void appendVarStatus(
            StringBuilder outStatus,
            Script1 script,
            ExecEngine1 engine,
            KeyValueServer kvServer) {

        outStatus.append("Freq: ");
        outStatus.append(String.format("%.1f Hz  [%.1f Hz]\n\n",
                engine.getActualFrequency(),
                engine.getMaxFrequency()));

        outStatus.append("--- [ TURNOUTS ] ---\n");
        int i = 0;
        for (String name : script.getTurnoutNames()) {
            Turnout turnout = script.getTurnout(name);
            outStatus.append(name.toUpperCase()).append(": ").append(turnout.isActive() ? 'N' : 'R');
            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        appendNewLine(outStatus);

        outStatus.append("--- [ SENSORS ] ---\n");
        i = 0;
        for (String name : script.getSensorNames()) {
            Sensor sensor = script.getSensor(name);
            outStatus.append(name.toUpperCase()).append(": ").append(sensor.isActive() ? '1' : '0');
            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        appendNewLine(outStatus);

        outStatus.append("--- [ TIMERS ] ---\n");
        i = 0;
        for (String name : script.getTimerNames()) {
            Timer timer = script.getTimer(name);
            outStatus.append(name).append(':').append(timer.isActive() ? '1' : '0');
            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        appendNewLine(outStatus);

        outStatus.append("--- [ ENUMS ] ---\n");
        i = 0;
        for (String name : script.getEnumNames()) {
            Enum_ enum_ = script.getEnum(name);
            outStatus.append(name).append(':').append(enum_.get());
            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        appendNewLine(outStatus);

        outStatus.append("--- [ VARS ] ---\n");
        i = 0;
        for (String name : script.getVarNames()) {
            Var var = script.getVar(name);
            outStatus.append(name).append(':').append(var.getAsInt());
            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        appendNewLine(outStatus);

        outStatus.append("--- [ KV Server ] ---\n");
        outStatus.append("Connections: ").append(kvServer.getNumConnections()).append('\n');
        for (String key : kvServer.getKeys()) {
            outStatus.append('[').append(key).append("] = ").append(kvServer.getValue(key)).append('\n');
        }
    }

    private static void appendNewLine(StringBuilder outStatus) {
        if (outStatus.charAt(outStatus.length() - 1) != '\n') {
            outStatus.append('\n');
        }
    }

    private void logln(String line) {
        if (mLogger == null) {
            System.out.println(TAG + " " + line);
        } else {
            mLogger.d(TAG, line);
        }
    }


    private void sendEvent(String action) {
        mAnalytics.sendEvent("Conductor", action, "", "Conductor");
    }

    @Singleton
    @Component(modules = { CommonModule.class })
    public interface LocalComponent1 extends IEngine1Component {
        IScript1Component.Factory getScriptComponentFactory();

        void inject(EntryPoint2 entryPoint);

        @Component.Factory
        interface Factory {
            LocalComponent1 createComponent(@BindsInstance IJmriProvider jmriProvider);
        }
    }
}
