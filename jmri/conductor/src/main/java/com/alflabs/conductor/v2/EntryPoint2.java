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
import com.alflabs.conductor.v1.dagger.IEngine1Component;
import com.alflabs.conductor.v1.dagger.IScriptComponent;
import com.alflabs.conductor.v1.script.ExecEngine;
import com.alflabs.conductor.v2.ui.IWindowCallback;
import com.alflabs.conductor.v2.ui.StatusWindow2;
import com.alflabs.kv.KeyValueServer;
import com.alflabs.utils.IClock;
import com.alflabs.utils.ILogger;
import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class EntryPoint2 implements IEntryPoint, IWindowCallback {
    private static final String TAG = EntryPoint2.class.getSimpleName();

    private boolean mIsSimulation;
    private LocalComponent mComponent;
    private StatusWindow2 mWin;
    private ExecEngine mEngine;
    private Thread mHandleThread;
    private final AtomicBoolean mKeepRunning = new AtomicBoolean(true);
    private final AtomicBoolean mPaused = new AtomicBoolean();

    @Inject ILogger mLogger;
    @Inject IClock mClock;
    @Inject KeyValueServer mKeyValueServer;
    @Inject EventLogger mEventLogger;
    @Inject Analytics mAnalytics;
    @Inject JsonSender mJsonSender;
    @Inject @Named("script") File mScriptFile;

    /** Entry point invoked from DevEntryPoint2. */
    public void init(@Null String simulationScript) {
        mIsSimulation = true;
        FakeJmriProvider jmriProvider = new FakeJmriProvider();
        setup(jmriProvider, simulationScript);
    }

    /** Entry point invoked directly from JMRI Jython Conductor.py. */
    @Override
    public boolean setup(IJmriProvider jmriProvider, String scriptPath) {
        File scriptFile = new File(scriptPath);
        mComponent = DaggerEntryPoint2_LocalComponent
                .factory()
                .createComponent(jmriProvider, scriptFile);

        // Do not use any injected field before this call
        mComponent.inject(this);

        logln("Setup");

        String eventLogFilename = mEventLogger.start(null);
        logln("Event log: " + eventLogFilename);

        openWindow();
        onWindowReload();

        return true;
    }

    /** Invoked from JMRI Jython Conductor.py as a loop or from the simulated thread. */
    @Override
    public boolean handle() {
        if (!mKeepRunning.get()) {
            logln("Stop Requested");
            return false;
        }

        if (mEngine == null || mPaused.get()) {
            // TODO poor's man async handling.
            // Consider some kind of CountDownLatch or a long monitor/notify or similar
            // instead of an active wait.
            try {
                Thread.sleep(330 /*ms*/);
            } catch (InterruptedException ignore) {}
            return true;
        }

        if (mEngine != null) {
            mEngine.onExecHandle();
        }
        return true;
    }

    public void runDevLoop() {
        mHandleThread = new Thread(this::_simulHandleThread, "EntryPoint2-HandleThread");
        mHandleThread.start();
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

    private void openWindow() {
        // TODO: opening the window can fail if this runs from a terminal or as a service.
        try {
            if (GraphicsEnvironment.isHeadless()) {
                logln("StatusWindow2 skipped: headless graphics environment");
            } else {
                mWin = new StatusWindow2();

                mWin.open(this);
            }
        } catch (Exception e) {
            logln("StatusWindow2 Failed: " + ExceptionUtils.getStackTrace(e));
            mWin = null;
        }
    }

    @Override
    public void onQuit() {
        logln("onQuit");
        mWin = null;
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

//        if (mWinUpdateThread != null) {
//            try {
//                mWinUpdateThread.join();
//                logln("Window Update Thread terminated");
//            } catch (InterruptedException e) {
//                logln("Window Update Thread join: " + e);
//            } finally {
//                mWinUpdateThread = null;
//            }
//        }

        if (mIsSimulation) {
            logln("Exit Simulation");
            System.exit(0);
        }
    }

    @Override
    public void onWindowReload() {
        logln("onWindowReload");

//        mUiUpdaters.clear();
//
//        if (mScriptAccess.mScriptComponent != null) {
//            // TODO release resources from current script component.
//            mScriptAccess.reset();
//        }

        if (mWin != null) {
            mWin.updateScriptName(mScriptFile.getName());
        }

//        try {
//            IScript2Component scriptComp = mScriptLoader.execByName(mScriptPath);
//            mScriptAccess.set(scriptComp);
//        } catch (Exception e) {
//            mLastError.setLength(0);
//            mLastError.append(e).append('\n').append(ExceptionUtils.getStackTrace(e));
//            if (mWin == null) {
//                logln("Parsing Exception: " + ExceptionUtils.getStackTrace(e));
//            }
//        }
//
//        registerUiThrottles();
//        registerUiConditionals();
//
//        mScriptAccess.mExecEngine2.onExecStart();
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

    private void logln(String line) {
        if (mLogger == null) {
            System.out.println(TAG + " " + line);
        } else {
            mLogger.d(TAG, line);
        }
    }

    @Singleton
    @Component(modules = { CommonModule.class })
    public interface LocalComponent extends IEngine1Component {
        IScriptComponent.Factory newScriptComponent();

        void inject(EntryPoint2 entryPoint);

        @Component.Factory
        interface Factory {
            LocalComponent createComponent(
                    @BindsInstance IJmriProvider jmriProvider,
                    @BindsInstance @Named("script") File scriptFile);
        }
    }
}
