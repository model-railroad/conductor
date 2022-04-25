package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.dsl.IActiveRoute
import com.alfray.conductor.v2.script.dsl.IActiveRouteBuilder

internal class ActiveRoute(activeRouteBuilder: IActiveRouteBuilder) : IActiveRoute {
    val routes = activeRouteBuilder.routes
}
