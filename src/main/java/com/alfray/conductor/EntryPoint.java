package com.alfray.conductor;

import com.alfray.conductor.parser.ScriptParser;
import com.alfray.conductor.script.Script;
import com.alfray.conductor.ui.StatusWnd;
import com.alfray.conductor.util.LogException;
import com.alfray.conductor.util.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/** Interface controlled by Conductor.py */
public class EntryPoint {
    private IJmriProvider mJmriProvider;
    private IJmriThrottle mThrottle;
    private Script mScript;

    /**
     * Invoked when the JMRI automation is being setup, from the Jython script.
     *
     * @return True to start the automation, false if there's a problem and it should not start.
     */
    public boolean setup(IJmriProvider jmriProvider, String scriptFile) {
        Logger logger = jmriProvider;
        logger.log("Conductor: Setup");
        File filepath = new File(scriptFile);
        final AtomicBoolean errorReported = new AtomicBoolean(false);
        try {
            mScript = new ScriptParser().parse(filepath, new ScriptParser.Reporter(logger) {
                @Override
                public void report(String line, int lineCount, String error) {
                    super.report(line, lineCount, error);
                    errorReported.set(true);
                }
            });
            mScript.setup(jmriProvider);
        } catch (IOException e) {
            errorReported.set(true);
            logger.log("[Conductor] Script Path: " + scriptFile);
            logger.log("[Conductor] Full Path: " + filepath.getAbsolutePath());
            logger.log("[Conductor] failed to load event script with the following exception:");
            LogException.logException(logger, e);
        }
        if (errorReported.get()) {
            return false;
        }

        // Open the window if a GUI is possible. This can fail.
        try {
            StatusWnd wnd = StatusWnd.open();
            // TODO associate listener to mScript

        } catch (Exception e) {
            // Ignore. continue.
            logger.log("Conductor UI not enabled: ");
            LogException.logException(logger, e);
        }

        return true;
    }

    /**
     * Invoked repeatedly by the automation Jython handler if {@link #setup(IJmriProvider, String)}
     * returned true.
     *
     * @return Will keep being called as long as it returns true.
     */
    public boolean handle() {
        System.out.println("Conductor: Handle");
        // TODO: if wnd != null && wnd.mMustStop then { wnd.close; wnd = null; return false; }
        if (mScript != null) {
            mScript.handle();
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) {
        // For testing purposes.
        StatusWnd.open();
    }

}
