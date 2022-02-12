package com.alflabs.conductor.v2.script

class Turnout {
    private final String mSystemName
    boolean mActive

    Turnout(String systemName) {
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
