/*
 * Project: Conductor
 * Copyright (C) 2017 alf.labs gmail com,
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

package com.alflabs.conductor;

import com.alflabs.conductor.util.Analytics;
import com.alflabs.conductor.util.JsonSender;
import com.alflabs.conductor.v1.parser.Reporter;
import com.alflabs.conductor.v1.parser.ScriptParser2;
import com.alflabs.conductor.v1.script.ExecEngine;
import com.alflabs.conductor.v1.script.IScriptComponent;
import com.alflabs.conductor.v1.script.Script;
import com.alflabs.conductor.v1.script.ScriptModule;
import com.alflabs.conductor.v1.simulator.Simulator;
import com.alflabs.conductor.v1.ui.StatusWnd;
import com.alflabs.conductor.util.EventLogger;
import com.alflabs.conductor.util.LogException;
import com.alflabs.kv.KeyValueServer;
import com.alflabs.manifest.Constants;
import com.alflabs.utils.ILogger;
import com.alflabs.utils.RPair;

import javax.inject.Inject;
import javax.jmdns.JmDNS;
import javax.jmdns.NetworkTopologyDiscovery;
import javax.jmdns.ServiceInfo;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** Interface controlled by Conductor.py */
public class EntryPoint {
    private static final String TAG = EntryPoint.class.getSimpleName();

    private Script mScript;
    private ExecEngine mEngine;
    private boolean mStopRequested;
    private IConductorComponent mComponent;
    private CountDownLatch mJmDNSLatch;
    private final List<JmDNS> mJmDnsList = new ArrayList<>();

    @Inject ILogger mLogger;
    @Inject KeyValueServer mKeyValueServer;
    @Inject EventLogger mEventLogger;
    @Inject Analytics mAnalytics;
    @Inject JsonSender mJsonSender;

    public Script getScript() {
        return mScript;
    }

    public ExecEngine getEngine() {
        return mEngine;
    }

    /**
     * Invoked when the JMRI automation is being setup, from the Jython script.
     *
     * @return True to start the automation, false if there's a problem and it should not start.
     */
    public boolean setup(IJmriProvider jmriProvider, String scriptPath) {

        // FIXME: if "DaggerIConductorComponent" cannot be resolved, 2 things are needed.
        // 1- Build the project.
        // 2- In Intellij, right click build/generated/source/apt/main and mark it as a
        //    generated source folder.
        // For some reason, IJ extracts generated/source/apt/main as a gen folder from the
        // gradle API instead of the proper build/generated/...
        File scriptFile = new File(scriptPath);
        mComponent = DaggerIConductorComponent
                .builder()
                .conductorModule(new ConductorModule(jmriProvider))
                .scriptFile(scriptFile)
                .build();

        // Do not use any injected field before this call
        mComponent.inject(this);

        mLogger.d(TAG, "Setup");

        String eventLogFilename = mEventLogger.start(null);
        mLogger.d(TAG, "Event log: " + eventLogFilename);

        if (loadScript().length() > 0) {
            return false;
        }

        new Thread(() -> startZeroconfAdvertising(scriptFile.getName())).start();

        // Open the window if a GUI is possible. This can fail.
        try {
            InetSocketAddress address = mKeyValueServer.start(Constants.KV_SERVER_PORT);
            mLogger.d(TAG, "KV Server available at " + address);

            StatusWnd wnd = StatusWnd.open();
            wnd.init(
                    mComponent,
                    mScript,
                    mEngine,
                    this::onReloadAction,
                    this::onStopAction,
                    getSimulator(mComponent));

        } catch (Exception e) {
            // Ignore. continue.
            mLogger.d(TAG, "UI not enabled: ");
            LogException.logException(mLogger, TAG, e);
        }

        return true;
    }

    protected Simulator getSimulator(IConductorComponent component) {
        return null;
    }

    private void sendEvent(String action) {
        mAnalytics.sendEvent("Conductor", action, "", "Conductor");
    }

    private void startZeroconfAdvertising(String name) {
        mJmDNSLatch = new CountDownLatch(1);
        try {
            mLogger.d(TAG, "Starting ZeroConf");

            Map<String, String> props = new TreeMap<>();
            props.put("origin", "conductor");
            props.put("version", "1");
            props.put("script", name);
            final int weight = 0;
            final int priority = 0;

            boolean gotIpv4 = false;
            final NetworkTopologyDiscovery topology = NetworkTopologyDiscovery.Factory.getInstance();
            for (InetAddress address : topology.getInetAddresses()) {
                if (!gotIpv4 && address instanceof Inet4Address) {
                    gotIpv4 = true;
                }
                if (gotIpv4 && address instanceof Inet6Address) {
                    mLogger.d(TAG, "Skip ZeroConf on " + address + " (already got an IPv4 before).");
                    continue;
                }
                ServiceInfo info = ServiceInfo.create(
                        Constants.KV_SERVER_SERVICE_TYPE,
                        name.replace(".", "-"),
                        Constants.KV_SERVER_PORT,
                        weight,
                        priority,
                        props);

                JmDNS jmDns = JmDNS.create(address);
                jmDns.registerService(info);
                mLogger.d(TAG, "Started ZeroConf on " + jmDns.getInetAddress());
                mJmDnsList.add(jmDns);
            }
        } catch (IOException e) {
            // Ignore. continue.
            mLogger.d(TAG, "ZeroConf not enabled: ");
            LogException.logException(mLogger, TAG, e);
        } finally {
            mJmDNSLatch.countDown();
        }
    }

    protected void onStopAction() {
        mJsonSender.sendEvent("conductor", null, "off");
        sendEvent("Stop");
        mLogger.d(TAG, "KV Server stopping, port " + Constants.KV_SERVER_PORT);
        if (mJmDNSLatch != null) {
            try {
                mLogger.d(TAG, "Waiting for ZeroConf");
                mJmDNSLatch.await(1, TimeUnit.MINUTES);
            } catch (InterruptedException ignore) {}
        }

        for (JmDNS jmDns : mJmDnsList) {
            try {
                mLogger.d(TAG, "Teardown ZeroConf on " + jmDns.getInetAddress());
            } catch (IOException ignore) {}
            jmDns.unregisterAllServices();
            try {
                jmDns.close();
            } catch (IOException e) {
                mLogger.d(TAG, "Teardown ZeroConf exception: " + e);
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
            mEventLogger.shutdown();
        } catch (InterruptedException e) {
            mLogger.d(TAG, "EventLogger Shutdown exception: " + e);
        }

        mKeyValueServer.stopSync();
        mStopRequested = true;
    }

    protected RPair<EntryPoint, String> onReloadAction() {
        sendEvent("Reload");
        String error = loadScript();
        return RPair.create(this, error);
    }

    private String loadScript() {
        StringBuilder error = new StringBuilder();
        Reporter reporter = new Reporter(mLogger) {
            private boolean mIsReport;

            @Override
            public void report(String line, int lineCount, String error) {
                mIsReport = true;
                super.report(line, lineCount, error);
                mIsReport = false;
            }

            @Override
            public void log(String msg) {
                super.log(msg);
                if (mIsReport) {
                    error.append(msg);
                    if (msg.length() > 0 && !msg.endsWith("\n")) {
                        error.append('\n');
                    }
                }
            }
        };

        IScriptComponent scriptComponent = mComponent.newScriptComponent(
                new ScriptModule(reporter, mKeyValueServer));

        try {
            ScriptParser2 parser = scriptComponent.createScriptParser2();
            // Remove existing script and try to reload, which may fail with an error.
            mEngine = null;
            mScript = parser.parse(mComponent.getScriptFile());
            mEngine = scriptComponent.createScriptExecEngine();
            mEngine.onExecStart();
            sendEvent("Start");
            mJsonSender.sendEvent("conductor", null, "on");
        } catch (IOException e) {
            mLogger.d(TAG, "Script Path: " + mComponent.getScriptFile().getAbsolutePath());
            mLogger.d(TAG, "Failed to load event script with the following exception:");
            LogException.logException(mLogger, TAG, e);
        }
        return error.toString();
    }

    /**
     * Invoked repeatedly by the automation Jython handler if {@link #setup(IJmriProvider, String)}
     * returned true.
     *
     * @return Will keep being called as long as it returns true.
     */
    @SuppressWarnings("unused")
    public boolean handle() {
        // DEBUG ONLY: mScript.getLogger().log("Handle");
        if (mStopRequested) {
            mLogger.d(TAG, "Stop Requested");
            return false;
        }
        if (mEngine != null) {
            mEngine.onExecHandle();
        }
        return true;
    }

    public static void main(String[] args) {
        // For testing purposes.
        StatusWnd.open();
    }
}
