package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.dsl.IActiveRoute
import com.alfray.conductor.v2.script.dsl.IRoute
import com.alfray.conductor.v2.script.dsl.IRouteSequenceBuilder
import com.alfray.conductor.v2.script.dsl.RouteSequenceBuilder

internal class ActiveRoute : IActiveRoute {
    private var _active: IRoute? = null

    val routes = mutableListOf<IRoute>()
    override val active: IRoute
        get() = _active!!

    override fun activate(route: IRoute) {
        check(route in routes)
        _active = route
    }

    private fun add(route: IRoute): IRoute {
        check(route !in routes)
        routes.add(route)
        if (_active == null) { _active = route }
        return route
    }

    override fun idle(): IRoute {
        return add(RouteIdle())
    }

    override fun sequence(init: IRouteSequenceBuilder.() -> Unit): IRoute {
        val builder = RouteSequenceBuilder(this)
        builder.init()
        return add(builder.create())
    }
}
