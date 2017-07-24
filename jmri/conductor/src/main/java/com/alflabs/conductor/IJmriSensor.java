package com.alflabs.conductor;

/** Abstraction of a JMRI sensor. */
public interface IJmriSensor {
    /** True when the sensor is valid and active. */
    boolean isActive();
    /**
     * Overrides the state of the JMRI sensor.
     * This works when using the DevelopmentEntryPoint and may not do anything when
     * running with a real JMRI instance (the sensor state will be reset when the
     * sensor is updated by JMRI).
     */
    void setActive(boolean active);
}
