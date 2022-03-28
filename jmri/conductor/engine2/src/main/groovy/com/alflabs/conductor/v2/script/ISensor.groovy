package com.alflabs.conductor.v2.script

import com.alflabs.annotations.NonNull

interface ISensor extends IActive {
    @NonNull
    String getSystemName()
}
