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
import com.alflabs.conductor.jmri.FakeJmriProvider;
import com.alflabs.conductor.jmri.IJmriProvider;
import com.alflabs.conductor.util.Analytics;
import com.alflabs.conductor.util.EventLogger;
import com.alflabs.conductor.util.JsonSender;
import com.alflabs.conductor.util.LogException;
import com.alflabs.conductor.util.Pair;
import com.alflabs.conductor.v2.ui.IWindowCallback;
import com.alflabs.conductor.v2.ui.StatusWindow2;
import com.alflabs.kv.KeyValueServer;
import com.alflabs.manifest.MapInfo;
import com.alflabs.utils.IClock;
import com.alflabs.utils.ILogger;

import javax.inject.Inject;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class EntryPoint2 implements IEntryPoint, IWindowCallback {
    private static final String TAG = EntryPoint2.class.getSimpleName();

    private boolean mIsSimulation;
    private Engine1Adapter.LocalComponent1 mComponent;
    private StatusWindow2 mWin;
    private Thread mHandleThread;
    private Thread mWinUpdateThread;
    private final StringBuilder mStatus = new StringBuilder();
    private final StringBuilder mLoadError = new StringBuilder();
    private final AtomicBoolean mKeepRunning = new AtomicBoolean(true);
    private final AtomicBoolean mPaused = new AtomicBoolean();
    private final Engine1Adapter mAdapter = new Engine1Adapter();

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
        FakeJmriProvider jmriProvider = new FakeJmriProvider();
        setup(jmriProvider, simulationScript);
    }

    /**
     * Entry point invoked directly from JMRI Jython Conductor.py.
     */
    @Override
    public boolean setup(IJmriProvider jmriProvider, String scriptPath) {
        File scriptFile = new File(scriptPath);
        mComponent = DaggerEngine1Adapter_LocalComponent1
                .factory()
                .createComponent(jmriProvider);

        // Do not use any injected field before this call
        mComponent.inject(this);
        mComponent.inject(mAdapter);
        mAdapter.setScriptFile(scriptFile);

        log("Setup");

        String eventLogFilename = mEventLogger.start(null);
        log("Event log: " + eventLogFilename);

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
            log("Stop Requested");
            return false;
        }

        mAdapter.onHandle(mPaused);
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

//                    try {
//                        mUiUpdaters.values().forEach(r -> {
//                            try {
//                                r.run();
//                            } catch (Exception e) {
//                                log("Window Update Thread - Exception (ignored): "
//                                        + ExceptionUtils.getStackTrace(e));
//                            }
//                        });
//                    } catch (ConcurrentModificationException ignore) {
//                    } catch (Exception e2) {
//                        log("Window Update Thread - Exception (ignored): "
//                                + ExceptionUtils.getStackTrace(e2));
//                    }
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

        // TBD: mUiUpdaters.clear();

        try {
            Pair<Boolean, File> reloaded = mAdapter.onReload();
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

        // TBD: registerUiThrottles();
        // TBD: registerUiConditionals();
    }

    private void loadMap() {
        if (!mAdapter.getScriptFile().isPresent()) {
            return;
        }
        File scriptFile = mAdapter.getScriptFile().get();
        File scriptDir = scriptFile.getParentFile();

        Optional<MapInfo> mapName = mAdapter.getLoadedMapName();

        if (mapName.isPresent()) {
            String svgName = mapName.get().getUri();
            File svgFile = scriptDir == null ? new File(svgName) : new File(scriptDir, svgName);
            URI svgUri = svgFile.toURI();

            log("Loading map: " + svgUri);
            try {
                mWin.displaySvgMap(svgUri);
            } catch (Exception e) {
                log("Failed to load map '" + svgName + "' : " + e);
            }
        }
    }

    @Override
    public void onWindowPause() {
        log("onWindowPause");
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

        mAdapter.appendToLog(mStatus, mKeyValueServer);

        mWin.updateLog(mStatus.toString());
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
}
