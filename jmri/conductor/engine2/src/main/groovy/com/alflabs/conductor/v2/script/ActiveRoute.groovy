package com.alflabs.conductor.v2.script

class ActiveRoute extends BaseVar {
    private final ActiveRouteInfo mInfo
    private Route mActiveRoute

    ActiveRoute(ActiveRouteInfo info) {
        this.mInfo = info
        this.mActiveRoute = null
    }

    Route[] getRoutes() {
        return mInfo.mRoutes
    }

    List<IRule> evaluateRules() {
        if (mActiveRoute != null) {
            return mActiveRoute.evaluateRules()
        }
        return Collections.emptyList()
    }
}
