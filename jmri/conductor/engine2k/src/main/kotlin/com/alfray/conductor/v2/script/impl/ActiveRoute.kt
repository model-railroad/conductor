package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.TAction
import com.alfray.conductor.v2.script.dsl.IActiveRoute
import com.alfray.conductor.v2.script.dsl.IActiveRouteBuilder
import com.alfray.conductor.v2.script.dsl.IRoute
import com.alfray.conductor.v2.script.dsl.IRouteSequenceBuilder
import com.alfray.conductor.v2.script.dsl.RouteSequenceBuilder
import com.alfray.conductor.v2.script.dsl.RuleActionEmpty

internal class ActiveRouteBuilder : IActiveRouteBuilder {
    var actionOnError = RuleActionEmpty

    override fun onError(action: TAction) {
        check(actionOnError == RuleActionEmpty)
        actionOnError = action
    }

    fun create() : IActiveRoute = ActiveRoute(this)
}

internal class ActiveRoute(builder: ActiveRouteBuilder) : IActiveRoute {
    private var _active: IRoute? = null
    private var _error: Boolean = false
    private val actionOnError = builder.actionOnError

    val routes = mutableListOf<IRoute>()
    override val active: IRoute
        get() = _active!!
    override val error: Boolean
        get() = _error

    override fun activate(route: IRoute) {
        check(route in routes)
        _active = route
    }

    fun execOnError() {
        actionOnError.invoke()
    }

    private fun add(route: IRoute): IRoute {
        check(route !in routes)
        routes.add(route)
        if (_active == null) { _active = route }
        return route
    }

    override fun idle(): IRoute {
        return add(RouteIdle(this))
    }

    override fun sequence(init: IRouteSequenceBuilder.() -> Unit): IRoute {
        val builder = RouteSequenceBuilder(this)
        builder.init()
        return add(builder.create())
    }
}
