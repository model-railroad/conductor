package com.alflabs.conductor.v2.script

class ActiveRoute extends BaseVar {
    private final ActiveRouteInfo mInfo

    ActiveRoute(ActiveRouteInfo info) {
        this.mInfo = info
    }

    Route[] getRoutes() {
        return mInfo.mRoutes
    }
}
