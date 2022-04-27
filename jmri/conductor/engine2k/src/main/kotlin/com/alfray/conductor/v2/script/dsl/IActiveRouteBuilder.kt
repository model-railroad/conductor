package com.alfray.conductor.v2.script.dsl

import com.alfray.conductor.v2.script.TAction

interface IActiveRouteBuilder {
    fun onError(action: TAction)
}
