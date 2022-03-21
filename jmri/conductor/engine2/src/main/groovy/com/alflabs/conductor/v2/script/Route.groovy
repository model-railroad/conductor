package com.alflabs.conductor.v2.script

import com.alflabs.annotations.NonNull

class Route extends BaseVar {
    private final IRouteManager mManager

    Route(@NonNull IRouteManager manager) {
        mManager = manager
    }

    @NonNull
    IRouteManager getManager() {
        return mManager
    }

    @NonNull
    List<IRule> evaluateRules() {
        return mManager.evaluateRules()
    }
}
