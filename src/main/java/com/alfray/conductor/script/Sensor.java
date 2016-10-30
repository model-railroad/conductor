package com.alfray.conductor.script;

import com.alfray.conductor.IJmriSensor;

public class Sensor implements IConditional {

    private final String mName;
    private final IJmriSensor mSensor;

    public Sensor(String scriptName, IJmriSensor sensor) {
        mName = scriptName;
        mSensor = sensor;
    }

    @Override
    public boolean isActive() {
        return mSensor.isActive();
    }
}
