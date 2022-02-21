package com.alflabs.conductor.v2.script

class RouteInfo {
    private IRouteManager mManager

    void setManager(IRouteManager manager) {
        mManager = manager
    }

    void onActivate(@DelegatesTo(RootScript) Closure action) {
    }
}
