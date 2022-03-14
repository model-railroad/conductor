package com.alflabs.conductor.v2.script

class ActiveRouteInfo {
    final List<Route> mRoutes = new ArrayList<>()

    void setRoutes(Route[] routes) {
        mRoutes.addAll(routes)
    }
}
