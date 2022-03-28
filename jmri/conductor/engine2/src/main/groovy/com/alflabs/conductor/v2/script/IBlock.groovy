package com.alflabs.conductor.v2.script

import com.alflabs.annotations.NonNull

interface IBlock extends IActive {
    @NonNull
    String getSystemName()
}
