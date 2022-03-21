package com.alflabs.conductor.v2.script

import com.alflabs.annotations.NonNull

interface IRouteManager {
    @NonNull
    List<IRule> evaluateRules()
}
