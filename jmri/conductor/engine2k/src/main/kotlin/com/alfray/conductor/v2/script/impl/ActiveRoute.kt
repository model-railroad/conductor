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

import com.alfray.conductor.v2.script.ExecAction
import com.alfray.conductor.v2.script.ExecContext
import com.alfray.conductor.v2.script.dsl.IActiveRoute
import com.alfray.conductor.v2.script.dsl.IRoute
import com.alfray.conductor.v2.script.dsl.IRouteSequenceBuilder

internal class ActiveRoute(
    builder: ActiveRouteBuilder
) : IActiveRoute, IExecEngine {
    private var _active: IRoute? = null
    private var _error: Boolean = false
    private val actionOnError = builder.actionOnError
    private val _routes = mutableListOf<IRoute>()
    private val context = ExecContext(ExecContext.State.ACTIVE_ROUTE)

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

    /**
     * Called by routes to indicate their error state has changed.
     * Current behavior is to always set the error, there's no current way to clear
     * it except by restarting the entire script.
     */
    fun reportError(route: IRoute, isError: Boolean) {
        if (isError) {
            _error = true
        }
    }

    inline fun assertOrError(value: Boolean, lazyMessage: () -> Any) {
        if (!value) {
            _error = true
            val message = lazyMessage()
            throw IllegalStateException(message.toString())
        }
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
        _error = false
        assertOrError(_routes.isNotEmpty()) {
            "An active route must contain at least one route definition, such as 'idle()'."
        }
        if (_active == null) {
            _active = _routes.first()
        }
        val route = _active
        if (route is IRouteManager) {
            route.initRoute()
        }
    }

    /** Invoked by the ExecEngine2 loop _before_ collecting all the actions to evaluate. */
    override fun onExecHandle() {
        val route = _active
        if (route is IRouteManager) {
            route.manageRoute()
        }
    }

    /** Invoked by the ExecEngine2 loop to collect all actions to evaluate. */
    fun collectActions(execActions: MutableList<ExecAction>) {
        if (error) {
            execActions.add(ExecAction(context, actionOnError))
            return
        }

        val route = _active
        if (route is RouteSequence) {
            route.collectActions(execActions)
        }
    }
}

