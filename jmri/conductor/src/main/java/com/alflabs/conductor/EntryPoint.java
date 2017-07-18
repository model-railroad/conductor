package com.alflabs.conductor;

import com.alflabs.conductor.parser.Reporter;
import com.alflabs.conductor.parser.ScriptParser2;
import com.alflabs.conductor.script.ExecEngine;
import com.alflabs.conductor.script.IScriptComponent;
import com.alflabs.conductor.script.Script;
import com.alflabs.conductor.script.ScriptModule;
import com.alflabs.conductor.ui.StatusWnd;
import com.alflabs.conductor.util.LogException;
import com.alflabs.conductor.util.Logger;
import com.alflabs.kv.KeyValueServer;
import com.alflabs.utils.RPair;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

/** Interface controlled by Conductor.py */
public class EntryPoint {
    private static final int KV_SERVER_PORT = 8080;

    private Script mScript;
    private ExecEngine mEngine;
    private boolean mStopRequested;
    private IConductorComponent mComponent;

    @Inject Logger mLogger;
    @Inject KeyValueServer mKeyValueServer;

    /**
     * Invoked when the JMRI automation is being setup, from the Jython script.
     *
     * @return True to start the automation, false if there's a problem and it should not start.
     */
    public boolean setup(IJmriProvider jmriProvider, String scriptFile) {

        mComponent = DaggerIConductorComponent
                .builder()
                .conductorModule(new ConductorModule(jmriProvider))
                .scriptFile(new File(scriptFile))
                .build();

        mComponent.inject(this);

        mLogger.log("[Conductor] Setup");
        if (loadScript().length() > 0) {
            return false;
        }

        // Open the window if a GUI is possible. This can fail.
        try {
            InetSocketAddress address = mKeyValueServer.start(KV_SERVER_PORT);
            mLogger.log("[Conductor] KV Server available at " + address);

            StatusWnd wnd = StatusWnd.open();
            wnd.init(
                    mComponent,
                    mScript,
                    mEngine,
                    // Reloader getter
                    () -> {
                        String error = loadScript();
                        return RPair.create(mScript, error);
                    },
                    // Stopper runnable
                    () -> {
                        mLogger.log("[Conductor] KV Server stopping, port " + KV_SERVER_PORT);
                        mKeyValueServer.stopSync();
                        mStopRequested = true;
                    });

        } catch (Exception e) {
            // Ignore. continue.
            mLogger.log("[Conductor] UI not enabled: ");
            LogException.logException(mLogger, e);
        }

        return true;
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
                new ScriptModule(reporter));

        try {
            ScriptParser2 parser = scriptComponent.getScriptParser2();
            // Remove existing script and try to reload, which may fail with an error.
            mEngine = null;
            mScript = null;
            parser.parse(mComponent.getScriptFile());

            mEngine = scriptComponent.getScriptExecEngine();
            mEngine.setup(mComponent.getJmriProvider());
        } catch (IOException e) {
            mLogger.log("[Conductor] Script Path: " + mComponent.getScriptFile().getAbsolutePath());
            mLogger.log("[Conductor] failed to load event script with the following exception:");
            LogException.logException(mLogger, e);
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
        // DEBUG ONLY: mScript.getLogger().log("[Conductor] Handle");
        if (mStopRequested) {
            mLogger.log("[Conductor] Stop Requested");
            return false;
        }
        if (mEngine != null) {
            mEngine.handle();
        }
        return true;
    }

    public static void main(String[] args) {
        // For testing purposes.
        StatusWnd.open();
    }

}
