package com.alfray.conductor.v2.script.dsl

import com.alfray.conductor.v2.script.TAction

interface IActiveRouteBuilder {
    /** Callback when a route becomes in error. */
    fun onError(action: TAction)
}
