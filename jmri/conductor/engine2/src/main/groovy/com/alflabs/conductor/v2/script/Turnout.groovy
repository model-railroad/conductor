package com.alflabs.conductor.v2.script

class Turnout extends BaseActive {
    private final String mSystemName
    private boolean mIsNormal = true

    Turnout(String systemName) {
        this.mSystemName = systemName
    }

    void normal() {
        mIsNormal = true
    }

    void reverse() {
        mIsNormal = false
    }

    boolean isNormal() {
        return mIsNormal
    }

}
