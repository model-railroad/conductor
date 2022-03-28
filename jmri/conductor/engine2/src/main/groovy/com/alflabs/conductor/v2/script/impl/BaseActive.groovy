package com.alflabs.conductor.v2.script.impl

import com.alflabs.conductor.v2.script.IActive

class BaseActive extends BaseVar implements IActive {
    private boolean mActive

    void setActive(boolean active) {
        this.mActive = active
    }

    @Override
    boolean isActive() {
        return mActive
    }

    boolean asBoolean() {
        return isActive()
    }
}
