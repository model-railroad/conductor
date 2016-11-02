package com.alfray.conductor;

/**
 * Interface to JMRI to provide sensor and throttle JMRI adapter objects.
 */
public interface IJmriProvider {
    /** Returns a new JMRI throttle adapter for the given DCC long address. */
    IJmriThrottle getThrotlle(int dccAddress);
    /** Returns a new JMRI sensor adapter for the given sensor system name. */
    IJmriSensor getSensor(String systemName);
}
