package com.alflabs.conductor.v2.script.impl

import com.alflabs.annotations.NonNull

interface IRouteManager {
    @NonNull
    List<IEvalRule> evaluateRules()
}
