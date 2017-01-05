package com.alflabs.conductor;

import com.alflabs.conductor.util.Logger;

/**
 * Interface to JMRI to provide JMRI adapter objects.
 */
public interface IJmriProvider extends Logger {
    /**
     * Returns a new JMRI throttle adapter for the given DCC long address.
     * Returns null if the object cannot be created.
     */
    IJmriThrottle getThrotlle(int dccAddress);

    /**
     * Returns a new JMRI sensor adapter for the given sensor system name.
     * Returns null if the object cannot be created.
     */
    IJmriSensor getSensor(String systemName);

    /**
     * Returns a new JMRI turnout adapter for the given turnout system name.
     * Returns null if the object cannot be created.
     */
    IJmriTurnout getTurnout(String systemName);
}
