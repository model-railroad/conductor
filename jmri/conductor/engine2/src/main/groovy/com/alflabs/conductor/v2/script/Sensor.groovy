package com.alflabs.conductor.v2.script

class Sensor {
    private final String mSystemName
    private boolean mActive

    Sensor(String systemName) {
        this.mSystemName = systemName
    }

    void setActive(boolean active) {
        this.mActive = active
    }

    boolean isActive() {
        return mActive
    }

    boolean asBoolean() {
        return isActive()
    }
}
