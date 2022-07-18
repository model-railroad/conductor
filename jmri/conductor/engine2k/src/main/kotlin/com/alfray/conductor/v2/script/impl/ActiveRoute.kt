/*
 * Project: Conductor
 * Copyright (C) 2022 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

internal class ActiveRoute(
    builder: ActiveRouteBuilder
) : IActiveRoute, IExecEngine {
    private var _active: IRoute? = null
    private var _error: Boolean = false
    private val actionOnError = builder.actionOnError
    private val _routes = mutableListOf<IRoute>()

    override val active: IRoute
        get() = _active!!
    override val routes: List<IRoute>
        get() = _routes
    override val error: Boolean
        get() = _error

    override fun activate(route: IRoute) {
        check(route in _routes)
        _active = route
    }

    fun execOnError() {
        actionOnError.invoke()
    }

    private fun add(route: IRoute): IRoute {
        check(route !in _routes)
        _routes.add(route)
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

    override fun onExecStart() {
        if (_active == null) {
            _active = _routes.firstOrNull()
        }
    }

    /** Invoked by the ExecEngine2 loop _before_ evaluating all the rules. */
    override fun onExecHandle() {
        val route = _active
        if (route != null && route is IRouteManager) {
            route.manageRoute()
        }
    }
}

