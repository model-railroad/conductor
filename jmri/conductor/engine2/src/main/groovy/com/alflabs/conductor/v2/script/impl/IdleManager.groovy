package com.alflabs.conductor.v2.script.impl

import com.alflabs.annotations.NonNull

class IdleManager implements IRouteManager {
    @Override
    @NonNull
    List<IEvalRule> evaluateRules() {
        return Collections.emptyList()
    }
}
