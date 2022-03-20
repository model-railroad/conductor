package com.alflabs.conductor.v2.script

class Route extends BaseVar {
    private final IRouteManager mManager

    Route(IRouteManager manager) {
        this.mManager = manager
    }

    IRouteManager getManager() {
        return mManager
    }

    List<IRule> evaluateRules() {
        return mManager.evaluateRules()
    }
}
