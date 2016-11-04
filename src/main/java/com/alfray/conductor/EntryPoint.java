package com.alfray.conductor;

import com.alfray.conductor.parser.ScriptParser;
import com.alfray.conductor.script.Script;
import com.alfray.conductor.ui.StatusWnd;

import java.io.File;
import java.io.IOException;

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
        System.out.println("Conductor: Setup");
        File filepath = new File(scriptFile);
        try {
            mScript = new ScriptParser().parse(filepath, new ScriptParser.Reporter());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // Open the window if a GUI is possible. This can fail.
        try {
            StatusWnd wnd = StatusWnd.open();
            // TODO associate listener to mScript

        } catch (Exception e) {
            // Ignore. continue.
            System.out.println("Conductor Script Path: " + scriptFile);
            System.out.println("Conductor Full Path: " + filepath.getAbsolutePath());
            System.out.println("Conductor failed to load event script with the following exception:");
            e.printStackTrace();
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
