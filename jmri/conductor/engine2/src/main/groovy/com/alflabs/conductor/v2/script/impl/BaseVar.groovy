package com.alflabs.conductor.v2.script.impl

import com.alflabs.annotations.NonNull

class BaseVar {
    private String mVarName

    void setVarName(@NonNull String varName) {
        mVarName = varName
    }

    @NonNull
    String getVarName() {
        return mVarName
    }
}
