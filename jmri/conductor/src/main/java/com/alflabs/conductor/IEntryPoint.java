package com.alflabs.conductor;

import com.alflabs.conductor.jmri.IJmriProvider;

public interface IEntryPoint {
    /**
     * Invoked when the JMRI automation is being setup, from the Jython script.
     *
     * @return True to start the automation, false if there's a problem and it should not start.
     */
    boolean setup(IJmriProvider jmriProvider, String scriptPath);

    /**
     * Invoked repeatedly by the automation Jython handler if {@link #setup(IJmriProvider, String)}
     * returned true.
     *
     * handle() is called repeatedly as long as it returns true.
     *
     * @return True to continue, false to stop.
     */
    boolean handle();
}
