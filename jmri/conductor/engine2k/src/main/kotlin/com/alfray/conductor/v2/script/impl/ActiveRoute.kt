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

import com.alflabs.utils.ILogger
import com.alfray.conductor.v2.script.ExecAction
import com.alfray.conductor.v2.script.ExecContext
import com.alfray.conductor.v2.script.dsl.IActiveRoute
import com.alfray.conductor.v2.script.dsl.IRoute
import com.alfray.conductor.v2.script.dsl.IRouteSequenceBuilder
import com.alfray.conductor.v2.utils.assertOrThrow


/**
 * An "active route" is a group of routes, of which one and only one is active at a given time.
 *
 * An active route must have at least one route associated with it. Routes can have different
 * characteristics -- e.g. an 'idle' route does absolutely nothing, whereas a 'sequence' route
 * implements a shuttle. Furthermore, the 'idle' exists with the sole purpose of having a default
 * no-op route when an active route must not be operating (since an active route must have an...
 * 'active' route, per definition).
 *
 * Routes have one state that matters to the active route:
 * - Idle: the route is not active and not being invoked by the script.
 * - Activated: the route has been activated. Its onActivated callback is called once.
 * - Active: the route is active and processing its normal behavior (e.g. sequence).
 * - Error: the route is in error. Its onRecover callback is called repeatedly.
 *
 * Routes are responsible for identifying their own error state. They do so by calling the
 * active route [reportError] method. This triggers the ActiveRoute's onError callback once,
 * after which the route's onRecover callback is used instead of the normal processing.
 */
internal class ActiveRoute(
    private var logger: ILogger,
    builder: ActiveRouteBuilder
) : IActiveRoute, IExecEngine {
    private val TAG = javaClass.simpleName
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
        logger.assertOrThrow(TAG, route in _routes) {
            "ERROR cannot active a route not part of an active route: $route"
        }
        _active = route
    }

    /**
     * Called by routes to indicate their error state has changed.
     * Current behavior is to always set the error, there's no current way to clear
     * it except by restarting the entire script.
     */
    fun reportError(route: IRoute, isError: Boolean) {
        if (isError) {
            logger.d(TAG, "Route entered error mode: $route")
            _error = true
        }
    }

    inline fun assertOrError(value: Boolean, lazyMessage: () -> Any) {
        if (!value) {
            _error = true
            val message = lazyMessage().toString()
            logger.d(TAG, message)
            throw IllegalStateException(message)
        }
    }

    private fun add(route: IRoute): IRoute {
        logger.assertOrThrow(TAG, route !in _routes) {
            "ERROR trying to add route already part of an active route: $route"
        }
        _routes.add(route)
        return route
    }

    override fun idle(): IRoute {
        return add(RouteIdle(this))
    }

    override fun sequence(init: IRouteSequenceBuilder.() -> Unit): IRoute {
        val builder = RouteSequenceBuilder(logger, this)
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
            actionOnError?.let {
                execActions.add(ExecAction(context, it))
            }
            return
        }

        val route = _active
        if (route is RouteSequence) {
            route.collectActions(execActions)
        }
    }
}

