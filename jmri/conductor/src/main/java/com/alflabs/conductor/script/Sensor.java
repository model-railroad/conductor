package com.alflabs.conductor.script;

import com.alflabs.manifest.Constants;
import com.alflabs.manifest.Prefix;
import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.IJmriSensor;
import com.alflabs.kv.IKeyValue;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

/**
 * A sensor defined by a script.
 * <p/>
 * The actual JMRI sensor is only assigned via the {@link #onExecStart()} method.
 */
@AutoFactory(allowSubclasses = true)
public class Sensor implements IConditional, IExecEngine {

    private final String mJmriName;
    private final String mKeyName;
    private final IJmriProvider mJmriProvider;
    private final IKeyValue mKeyValue;

    private Runnable mOnChangedListener;
    private IJmriSensor mSensor;
    private boolean mLastActive;

    /** Creates a new sensor for the given JMRI system name. */
    public Sensor(
            String jmriName,
            String scriptName,
            @Provided IJmriProvider jmriProvider,
            @Provided IKeyValue keyValue) {
        mJmriName = jmriName;
        mKeyName = Prefix.Sensor + scriptName;
        mJmriProvider = jmriProvider;
        mKeyValue = keyValue;
    }

    public void setOnChangedListener(Runnable onChangedListener) {
        mOnChangedListener = onChangedListener;
    }

    /** Initializes the underlying JMRI sensor. */
    @Override
    public void onExecStart() {
        mSensor = mJmriProvider.getSensor(mJmriName);
        onExecHandle();
    }

    @Override
    public boolean isActive() {
        return mSensor != null && mSensor.isActive();
    }

    @Override
    public void onExecHandle() {
        boolean active = isActive();
        mKeyValue.putValue(mKeyName, active ? Constants.On : Constants.Off, true /*broadcast*/);
        if (active != mLastActive && mOnChangedListener != null) {
            mLastActive = active;
            mOnChangedListener.run();
        }
    }

    public IJmriSensor getJmriSensor() {
        return mSensor;
    }
}
