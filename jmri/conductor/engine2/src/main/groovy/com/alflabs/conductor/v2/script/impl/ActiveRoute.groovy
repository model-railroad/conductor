package com.alflabs.conductor.v2.script.impl

import com.alflabs.annotations.NonNull

class ActiveRoute extends BaseVar {
    private final ActiveRouteInfo mInfo
    private Route mActiveRoute

    ActiveRoute(@NonNull ActiveRouteInfo info) {
        this.mInfo = info
        this.mActiveRoute = null
    }

    @NonNull
    Route[] getRoutes() {
        return mInfo.mRoutes
    }

    @NonNull
    List<IEvalRule> evaluateRules() {
        if (mActiveRoute != null) {
            return mActiveRoute.evaluateRules()
        }
        return Collections.emptyList()
    }
}
