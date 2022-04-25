package com.alfray.conductor.v2.script.dsl

import com.alfray.conductor.v2.script.impl.ActiveRoute

interface IActiveRoute {

}

interface IActiveRouteBuilder {
    var routes: List<IRoute>
}

internal class ActiveRouteBuilder : IActiveRouteBuilder {
    override var routes: List<IRoute> = emptyList()

    fun create() : IActiveRoute = ActiveRoute(this)
}
