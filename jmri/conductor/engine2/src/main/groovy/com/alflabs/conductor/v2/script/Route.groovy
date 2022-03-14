package com.alflabs.conductor.v2.script

class Route extends BaseVar {
    private final RouteInfo mRouteInfo
    private final IRouteManager mManager

    Route(IRouteManager manager) {
        this.mManager = manager
        // this.mRouteInfo = routeInfo
    }
}
