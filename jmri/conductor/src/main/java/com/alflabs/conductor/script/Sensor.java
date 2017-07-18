package com.alflabs.conductor.script;

import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.IJmriSensor;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

/**
 * A sensor defined by a script.
 * <p/>
 * The actual JMRI sensor is only assigned via the {@link #onExecStart()} method.
 */
@AutoFactory(allowSubclasses = true)
public class Sensor implements IConditional, IExecStart {

    private final String mJmriName;
    private final IJmriProvider mJmriProvider;

    private IJmriSensor mSensor;

    /** Creates a new sensor for the given JMRI system name. */
    public Sensor(String jmriName, @Provided IJmriProvider jmriProvider) {
        mJmriName = jmriName;
        mJmriProvider = jmriProvider;
    }

    /** Initializes the underlying JMRI sensor. */
    @Override
    public void onExecStart() {
        mSensor = mJmriProvider.getSensor(mJmriName);
    }

    @Override
    public boolean isActive() {
        return mSensor != null && mSensor.isActive();
    }
}
