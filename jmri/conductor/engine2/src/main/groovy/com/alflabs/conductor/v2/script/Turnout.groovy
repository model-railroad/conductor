package com.alflabs.conductor.v2.script

import com.alflabs.annotations.NonNull

class Turnout extends BaseActive {
    private final String mSystemName
    private boolean mIsNormal = true

    Turnout(@NonNull String systemName) {
        mSystemName = systemName
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
