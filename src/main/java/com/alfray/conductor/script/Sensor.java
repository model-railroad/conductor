package com.alfray.conductor.script;

import com.alfray.conductor.IJmriProvider;
import com.alfray.conductor.IJmriSensor;

public class Sensor implements IConditional {

    private final String mJmriName;
    private IJmriSensor mSensor;

    public Sensor(String jmriName) {
        mJmriName = jmriName;
    }

    public void init(IJmriProvider provider) {
        mSensor = provider.getSensor(mJmriName);
    }

    @Override
    public boolean isActive() {
        return mSensor != null && mSensor.isActive();
    }
}
