package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.dsl.IActiveRoute
import com.alfray.conductor.v2.script.dsl.IActiveRouteBuilder
import com.alfray.conductor.v2.script.dsl.IRoute

internal class ActiveRoute(activeRouteBuilder: IActiveRouteBuilder) : IActiveRoute {
    val routes = activeRouteBuilder.routes

    override fun activate(route: IRoute) {
        TODO("Not yet implemented")
    }
}
