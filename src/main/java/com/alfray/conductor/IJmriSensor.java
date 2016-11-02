package com.alfray.conductor;

/** Abstraction of a JMRI sensor. */
public interface IJmriSensor {
    /** True when the sensor is valid and active. */
    boolean isActive();
}
