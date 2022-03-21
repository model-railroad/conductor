package com.alflabs.conductor.v2.script

import com.alflabs.annotations.NonNull

class IdleManager implements IRouteManager {
    @Override
    @NonNull
    List<IRule> evaluateRules() {
        return Collections.emptyList()
    }
}
