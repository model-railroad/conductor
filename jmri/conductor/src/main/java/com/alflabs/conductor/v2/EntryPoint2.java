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
import com.alflabs.conductor.jmri.IJmriProvider;
import com.alflabs.conductor.util.Analytics;
import com.alflabs.conductor.util.EventLogger;
import com.alflabs.conductor.util.JsonSender;
import com.alflabs.conductor.util.LogException;
import com.alflabs.conductor.util.Pair;
import com.alflabs.conductor.v2.ui.IWindowCallback;
import com.alflabs.conductor.v2.ui.StatusWindow2;
import com.alflabs.kv.KeyValueServer;
import com.alflabs.utils.IClock;
import com.alflabs.utils.ILogger;
import com.alfray.conductor.v2.simulator.dagger.DaggerISimul2kComponent;
import com.alfray.conductor.v2.simulator.dagger.ISimul2kComponent;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import javax.inject.Inject;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.JTextComponent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class EntryPoint2 implements IEntryPoint, IWindowCallback {
    private static final String TAG = EntryPoint2.class.getSimpleName();

    private boolean mIsSimulation;
    private StatusWindow2 mWin;
    private Thread mHandleThread;
    private Thread mWinUpdateThread;
    private final StringBuilder mStatus = new StringBuilder();
    private final StringBuilder mLoadError = new StringBuilder();
    private final AtomicBoolean mKeepRunning = new AtomicBoolean(true);
    private final AtomicBoolean mPaused = new AtomicBoolean();
    private final Map<Object, Runnable> mUiUpdaters = new HashMap<>();
    private Optional<IEngineAdapter> mAdapter = Optional.empty();
    private Optional<ISimul2kComponent> mSimul2kComponent = Optional.empty();

    @Inject ILogger mLogger;
    @Inject IClock mClock;
    @Inject KeyValueServer mKeyValueServer;
    @Inject EventLogger mEventLogger;
    @Inject Analytics mAnalytics;
    @Inject JsonSender mJsonSender;

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
        String mode = guessEngineMode(scriptFile);

        if ("legacy".equals(mode)) {
            Engine1Adapter adapter = new Engine1Adapter();
            mAdapter = Optional.of(adapter);
            Engine1Adapter.LocalComponent1 component = DaggerEngine1Adapter_LocalComponent1
                    .factory()
                    .createComponent(jmriProvider);
            // Do not use any injected field before this call
            component.inject(this);
            component.inject(adapter);

        } else if ("groovy".equals(mode)) {
            throw new IllegalArgumentException("Groovy Conductor2 script is not supported.");

        } else if ("kts".equals(mode)) {
            Engine2KotlinAdapter adapter = new Engine2KotlinAdapter();
            mAdapter = Optional.of(adapter);
            Engine2KotlinAdapter.LocalComponent2k component = DaggerEngine2KotlinAdapter_LocalComponent2k
                    .factory()
                    .createComponent(jmriProvider);
            // Do not use any injected field before this call
            component.inject(this);
            component.inject(adapter);
            adapter.setSimulator(mSimul2kComponent.orElse(null));

        } else {
            log("Unknown engine mode " + mode);
            return false;
        }
        log("Engine mode: " + mode);

        mAdapter.get().setScriptFile(scriptFile);

        String eventLogFilename = mEventLogger.start(null);
        log("Event log: " + eventLogFilename);

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
                        + ExceptionUtils.getStackTrace(e));
            }
        }
        log("Simul Handle Thread - End");
    }

    private void _windowUpdateThread() {
        log("Window Update Thread - Start");

        while (mKeepRunning.get() && mWin != null) {
            try {
                SwingUtilities.invokeAndWait(() -> {
                    updateWindowLog();

                    try {
                        mUiUpdaters.values().forEach(r -> {
                            try {
                                r.run();
                            } catch (Exception e) {
                                log("Window Update Thread - Exception (ignored): "
                                        + ExceptionUtils.getStackTrace(e));
                            }
                        });
                    } catch (ConcurrentModificationException ignore) {
                    } catch (Exception e2) {
                        log("Window Update Thread - Exception (ignored): "
                                + ExceptionUtils.getStackTrace(e2));
                    }
                });

                Thread.sleep(330 /*ms*/);
            } catch (InterruptedException | InvocationTargetException ignore) {
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
                mWin = new StatusWindow2();
                mWin.open(this);
                mWin.updateScriptName("No Script1 Loaded");

                if (mWinUpdateThread == null) {
                    mWinUpdateThread = new Thread(this::_windowUpdateThread, "EntryPoint2-WinUpdate");
                    mWinUpdateThread.start();
                }
            }
        } catch (Exception e) {
            log("StatusWindow2 Failed: " + ExceptionUtils.getStackTrace(e));
            mWin = null;
        }
    }

    @Override
    public void onQuit() {
        log("onQuit");
        mJsonSender.sendEvent("conductor", null, "off");
        sendEvent("Stop");

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

        if (mWinUpdateThread != null) {
            // TODO sometimes the win update threads is still stuck on the Swing invokeAndWait.
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

        mUiUpdaters.clear();

        if (!mAdapter.isPresent()) {
            log("onWindowReload: no engine adapter.");
            return;
        }

        try {
            // Creates the daggers script component, loads the script, executes onExecStart.
            Pair<Boolean, File> reloaded = mAdapter.get().onReload();
            boolean wasRunning = reloaded.mFirst;
            File file = reloaded.mSecond;

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
            log("Failed to load event script with the following exception:");
            LogException.logException(mLogger, TAG, e);

            mLoadError.setLength(0);
            mLoadError.append(e).append('\n').append(ExceptionUtils.getStackTrace(e));
            if (mWin == null) {
                log("Parsing Exception: " + ExceptionUtils.getStackTrace(e));
            }
        }

        registerUiThrottles();
        registerUiConditionals();
    }

    private void loadMap() {
        mAdapter.ifPresent(adapter ->
            adapter.getScriptFile().ifPresent(scriptFile -> {
                adapter.getLoadedMapName().ifPresent(mapInfo -> {
                    String svgName = mapInfo.getName();
                    URI svgUri = URI.create(mapInfo.getUri());

                    log("Loading map '" + svgName + "' from : " + svgUri);
                    try {
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
        if (mWin != null) {
            mWin.updatePause(mPaused.get());
        }
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

        mAdapter.ifPresent(adapter -> adapter.appendToLog(mStatus));
        appendVarStatus(mStatus, mKeyValueServer);

        mWin.updateLog(mStatus.toString());
    }

    private static void appendVarStatus(
            StringBuilder outStatus,
            KeyValueServer kvServer) {

        outStatus.append("--- [ KV Server ] ---\n");
        outStatus.append("Connections: ").append(kvServer.getNumConnections()).append('\n');
        for (String key : kvServer.getKeys()) {
            String value = kvServer.getValue(key);
            if (value.startsWith("{") && value.length() > 50) {
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
        if (mWin == null) { return; }
        mWin.removeThrottles();

        mAdapter.ifPresent(adapter -> {
            adapter.getThrottles().forEach(throttleAdapter -> {
                JTextComponent ui = mWin.addThrottle(throttleAdapter.getName());
                setupThrottlePane(ui);

                registerUiUpdate(throttleAdapter,
                new ThrottleUpdater(
                        throttleAdapter,
                        () -> updateThrottlePane(ui, throttleAdapter)));

            });
        });
    }

    private void setupThrottlePane(JTextComponent textPane) {
        StyleContext context = new StyleContext();
        StyledDocument doc = new DefaultStyledDocument(context);

        Style d = context.getStyle(StyleContext.DEFAULT_STYLE);
        Style c = context.addStyle("c", d);
        c.addAttribute(StyleConstants.Alignment, StyleConstants.ALIGN_CENTER);
        Style b = context.addStyle("b", c);
        b.addAttribute(StyleConstants.Bold, true);

        textPane.setDocument(doc);
    }

    private void updateThrottlePane(JTextComponent textPane, IThrottleDisplayAdapter throttleAdapter) {
        int speed = throttleAdapter.getSpeed();

        String line1 = String.format("%s [%d] %s %d\n",
                throttleAdapter.getName(),
                throttleAdapter.getDccAddress(),
                speed < 0 ? " <= " : (speed == 0 ? " == " : " => "),
                speed);

        String line2 = String.format("L%s S%s",
                throttleAdapter.isLight() ? "+" : "-",
                throttleAdapter.isSound() ? "+" : "-");

        try {
            StyledDocument doc = (StyledDocument) textPane.getDocument();
            doc.remove(0, doc.getLength());
            doc.insertString(0, line1, null);
            doc.insertString(doc.getLength(), line2, doc.getStyle("b"));
            // everything is centered
            doc.setParagraphAttributes(0, doc.getLength(), doc.getStyle("c"), false /*replace*/);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerUiConditionals() {
        if (mWin == null) { return; }
        mWin.removeSensors();

        mAdapter.ifPresent(adapter -> {
            adapter.getSensors().forEach(sensorAdapter -> {
                JCheckBox ui = mWin.addSensor(sensorAdapter.getName());

                registerUiUpdate(sensorAdapter,
                        new ActivableUpdater(
                                sensorAdapter,
                                () -> ui.setSelected(sensorAdapter.isActive())
                        ));
                ui.addActionListener(actionEvent -> sensorAdapter.setActive(ui.isSelected()));
            });

            adapter.getBlocks().forEach(blockAdapter -> {
                registerUiUpdate(blockAdapter,
                        new ActivableUpdater(
                                blockAdapter,
                                () -> {
                                    String name = "S-" + blockAdapter.getName();
                                    mWin.setBlockColor(name, blockAdapter.isActive());
                                }
                        ));
            });

            adapter.getTurnouts().forEach(turnoutAdapter -> {
                registerUiUpdate(turnoutAdapter,
                        new ActivableUpdater(
                                turnoutAdapter,
                                () -> {
                                    boolean normal = turnoutAdapter.isActive();
                                    String name = "T-" + turnoutAdapter.getName();
                                    String N = name + "N";
                                    String R = name + "R";

                                    mWin.setTurnoutVisible(N,  normal);
                                    mWin.setTurnoutVisible(R, !normal);
                                }
                        ));
            });

        });
    }

    private void registerUiUpdate(Object ref, Runnable updater) {
        mUiUpdaters.put(ref, updater);
    }

    private static class ThrottleUpdater implements Runnable {
        private final IThrottleDisplayAdapter mThrottleAdapter;
        private final Runnable mUpdate;
        private int mLastSpeed = Integer.MAX_VALUE;

        public ThrottleUpdater(IThrottleDisplayAdapter throttleAdapter, Runnable update) {
            mThrottleAdapter = throttleAdapter;
            mUpdate = update;
            mUpdate.run();
        }

        @Override
        public void run() {
            int newSpeed = mThrottleAdapter.getSpeed();
            if (newSpeed != mLastSpeed) {
                mLastSpeed = newSpeed;
                mUpdate.run();
            }
        }
    }

    private static class ActivableUpdater implements Runnable {
        private final IActivableDisplayAdapter mActivable;
        private final Runnable mUpdate;
        private boolean mLastActive;

        public ActivableUpdater(IActivableDisplayAdapter activable, Runnable update) {
            mActivable = activable;
            mUpdate = update;
            mUpdate.run();
        }

        @Override
        public void run() {
            boolean newState = mActivable.isActive();
            if (newState != mLastActive) {
                mLastActive = newState;
                mUpdate.run();
            }
        }
    }

}
