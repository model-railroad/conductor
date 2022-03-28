package com.alflabs.conductor.v2.script.impl

import com.alflabs.annotations.NonNull
import com.alflabs.conductor.v2.script.IBlock

class Block extends BaseActive implements IBlock {
    private final String mSystemName

    Block(@NonNull String systemName) {
        mSystemName = systemName
    }

    @NonNull
    String getSystemName() {
        return mSystemName
    }
}
