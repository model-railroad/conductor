package com.alflabs.conductor.v2.script

import com.alflabs.annotations.NonNull

class Block extends BaseActive {
    private final String mSystemName

    Block(@NonNull String systemName) {
        mSystemName = systemName
    }

    @NonNull
    String getSystemName() {
        return mSystemName
    }
}
