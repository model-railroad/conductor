package com.alflabs.conductor.v2.script.impl

import com.alflabs.annotations.NonNull

class ActiveRouteInfo {
    final List<Route> mRoutes = new ArrayList<>()

    void setRoutes(@NonNull Route[] routes) {
        mRoutes.addAll(routes)
    }
}
