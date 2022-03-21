package com.alflabs.conductor.v2.script

import com.alflabs.annotations.NonNull

class Sensor extends  BaseActive {
    private final String mSystemName

    Sensor(@NonNull String systemName) {
        mSystemName = systemName
    }

    @NonNull
    String getSystemName() {
        return mSystemName
    }
}
