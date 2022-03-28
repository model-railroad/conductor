package com.alflabs.conductor.v2.script.impl

import com.alflabs.annotations.NonNull
import com.alflabs.conductor.v2.script.ISensor

class Sensor extends BaseActive implements ISensor {
    private final String mSystemName

    Sensor(@NonNull String systemName) {
        mSystemName = systemName
    }

    @NonNull
    String getSystemName() {
        return mSystemName
    }
}
