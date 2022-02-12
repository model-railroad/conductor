package com.alflabs.conductor.v2.script

class BaseActive extends BaseVar {
    private boolean mActive

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
