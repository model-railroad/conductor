package com.alflabs.conductor;

import com.alflabs.conductor.parser.ScriptParser;
import com.alflabs.conductor.script.Script;
import com.alflabs.conductor.ui.StatusWnd;
import com.alflabs.conductor.util.LogException;
import com.alflabs.conductor.util.Logger;
import com.alflabs.conductor.util.Pair;

import java.io.File;
import java.io.IOException;

/** Interface controlled by Conductor.py */
public class EntryPoint {
    private Script mScript;
    private boolean mStopRequested;

    /**
     * Invoked when the JMRI automation is being setup, from the Jython script.
     *
     * @return True to start the automation, false if there's a problem and it should not start.
     */
    public boolean setup(IJmriProvider jmriProvider, String scriptFile) {
        Logger logger = jmriProvider;
        logger.log("[Conductor] Setup");
        File filepath = new File(scriptFile);

        if (loadScript(jmriProvider, scriptFile, logger, filepath).length() > 0) {
            return false;
        }

        // Open the window if a GUI is possible. This can fail.
        try {
            StatusWnd wnd = StatusWnd.open();
            wnd.init(
                    filepath,
                    mScript,
                    jmriProvider,
                    () -> {
                        String error = loadScript(jmriProvider, scriptFile, logger, filepath);
                        return Pair.of(mScript, error);
                    },
                    () -> mStopRequested = true);

        } catch (Exception e) {
            // Ignore. continue.
            logger.log("[Conductor] UI not enabled: ");
            LogException.logException(logger, e);
        }

        return true;
    }

    private String loadScript(IJmriProvider jmriProvider, String scriptFile, final Logger logger, File filepath) {
        StringBuilder error = new StringBuilder();
        ScriptParser.Reporter reporter = new ScriptParser.Reporter(logger) {
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
            mScript = new ScriptParser().parse(filepath, reporter);
            mScript.setup(jmriProvider);
        } catch (IOException e) {
            logger.log("[Conductor] Script Path: " + scriptFile);
            logger.log("[Conductor] Full Path: " + filepath.getAbsolutePath());
            logger.log("[Conductor] failed to load event script with the following exception:");
            LogException.logException(logger, e);
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
            mScript.getLogger().log("[Conductor] Stop Requested");
            return false;
        }
        if (mScript != null) {
            mScript.handle();
            return true;
        } else {
            mScript.getLogger().log("[Conductor] Script.Handle returned false");
            return false;
        }
    }

    public static void main(String[] args) {
        // For testing purposes.
        StatusWnd.open();
    }

}
