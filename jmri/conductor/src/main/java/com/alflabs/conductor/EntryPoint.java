package com.alflabs.conductor;

import com.alflabs.conductor.parser.Reporter;
import com.alflabs.conductor.parser.ScriptParser2;
import com.alflabs.conductor.script.Script;
import com.alflabs.conductor.ui.StatusWnd;
import com.alflabs.conductor.util.LogException;
import com.alflabs.conductor.util.Logger;
import com.alflabs.kv.KeyValueServer;
import com.alflabs.utils.ILogger;
import com.alflabs.utils.RPair;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

/** Interface controlled by Conductor.py */
public class EntryPoint {
    private static final int KV_SERVER_PORT = 8080;

    private Script mScript;
    private boolean mStopRequested;
    private KeyValueServer mKeyValueServer;

    /**
     * Invoked when the JMRI automation is being setup, from the Jython script.
     *
     * @return True to start the automation, false if there's a problem and it should not start.
     */
    public boolean setup(IJmriProvider jmriProvider, String scriptFile) {

        DaggerIConductorComponent.builder().build();

        Logger logger = jmriProvider;
        logger.log("[Conductor] Setup");
        File filepath = new File(scriptFile);

        if (loadScript(jmriProvider, scriptFile, logger, filepath).length() > 0) {
            return false;
        }

        // Open the window if a GUI is possible. This can fail.
        try {
            mKeyValueServer = new KeyValueServer(createLogger(logger));
            InetSocketAddress address = mKeyValueServer.start(KV_SERVER_PORT);
            logger.log("[Conductor] KV Server available at " + address);

            StatusWnd wnd = StatusWnd.open();
            wnd.init(
                    filepath,
                    mScript,
                    jmriProvider,
                    // Reloader getter
                    () -> {
                        String error = loadScript(jmriProvider, scriptFile, logger, filepath);
                        return RPair.create(mScript, error);
                    },
                    // Stopper runnable
                    () -> {
                        logger.log("[Conductor] KV Server stopping, port " + KV_SERVER_PORT);
                        mKeyValueServer.stopSync();
                        mStopRequested = true;
                    });

        } catch (Exception e) {
            // Ignore. continue.
            logger.log("[Conductor] UI not enabled: ");
            LogException.logException(logger, e);
        }

        return true;
    }

    private String loadScript(IJmriProvider jmriProvider, String scriptFile, final Logger logger, File filepath) {
        StringBuilder error = new StringBuilder();
        Reporter reporter = new Reporter(logger) {
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

        try {
            // Remove existing script and try to reload, which may fail with an error.
            mScript = null;
            mScript = new ScriptParser2().parse(filepath, reporter);
            mScript.setup(jmriProvider);
        } catch (IOException e) {
            logger.log("[Conductor] Script Path: " + scriptFile);
            logger.log("[Conductor] Full Path: " + filepath.getAbsolutePath());
            logger.log("[Conductor] failed to load event script with the following exception:");
            LogException.logException(logger, e);
        }
        return error.toString();
    }

    private ILogger createLogger(Logger logger) {
        return new ILogger() {
            @Override
            public void d(String tag, String message) {
                logger.log(tag + ": " + message);
            }

            @Override
            public void d(String tag, String message, Throwable tr) {
                logger.log(tag + ": " + message + ": " + tr);
            }
        };
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
            mScript.getLogger().log("[Conductor] Stop Requested");
            return false;
        }
        if (mScript != null) {
            mScript.handle();
        }
        return true;
    }

    public static void main(String[] args) {
        // For testing purposes.
        StatusWnd.open();
    }

}
