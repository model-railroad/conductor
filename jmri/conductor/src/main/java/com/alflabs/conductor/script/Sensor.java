package com.alflabs.conductor.script;

import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.IJmriSensor;

/**
 * A sensor defined by a script.
 * <p/>
 * The actual JMRI sensor is only assigned via the {@link #setup(IJmriProvider)} method.
 */
public class Sensor implements IConditional {

    private final String mJmriName;
    private IJmriSensor mSensor;

    /** Creates a new sensor for the given JMRI system name. */
    public Sensor(String jmriName) {
        mJmriName = jmriName;
    }

    /** Initializes the underlying JMRI sensor. */
    public void setup(IJmriProvider provider) {
        mSensor = provider.getSensor(mJmriName);
    }

    @Override
    public boolean isActive() {
        return mSensor != null && mSensor.isActive();
    }
}
