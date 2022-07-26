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
import com.alfray.conductor.v2.script.dsl.IRouteIdleBuilder
import com.alfray.conductor.v2.script.dsl.IRouteSequenceBuilder
import com.alfray.conductor.v2.script.dsl.TAction
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
    private var _active: RouteBase? = null
    private val actionOnError = builder.actionOnError
    private var callOnError: TAction? = null
    private val _routes = mutableListOf<IRoute>()
    private val context = ExecContext(ExecContext.State.ACTIVE_ROUTE)

    override val active: IRoute
        get() = _active!!
    override val routes: List<IRoute>
        get() = _routes
    override val error: Boolean
        get() = _active?.state == RouteBase.State.ERROR

    /** Called by script to change the active route. No-op if route is already active. */
    override fun activate(route: IRoute) {
        logger.assertOrThrow(TAG, route in _routes) {
            "ERROR cannot active a route not part of an active route: $route"
        }

        if (_active === route) {
            return // no-op
        }

        _active?.let {
            it.state = RouteBase.State.IDLE
            logger.d(TAG, "Route is now idle: $it")
        }

        _active = route as RouteBase
        _active?.state = RouteBase.State.ACTIVATED
        logger.d(TAG, "Route is now activated: $route")
    }

    /** Called by routes to indicate their error state has changed. */
    fun reportError(route: IRoute, isError: Boolean) {
        if (route === _active) {
            logger.d(TAG, "Active Route $route reports error $isError")
            if (isError) {
                callOnError = actionOnError
                route.state = RouteBase.State.ERROR
            } else {
                callOnError = null
                route.state = RouteBase.State.ACTIVATED
            }
        } else {
            logger.d(TAG, "Ignore non-active Route $route reports error $isError")
        }
    }

    private fun add(route: IRoute): IRoute {
        logger.assertOrThrow(TAG, route !in _routes) {
            "ERROR trying to add route already part of an active route: $route"
        }
        _routes.add(route)
        return route
    }

    override fun idle(routeIdleSpecification: IRouteIdleBuilder.() -> Unit): IRoute {
        val builder = RouteIdleBuilder(logger, this)
        builder.routeIdleSpecification()
        return add(builder.create())
    }

    override fun sequence(routeSequenceSpecification: IRouteSequenceBuilder.() -> Unit): IRoute {
        val builder = RouteSequenceBuilder(logger, this)
        builder.routeSequenceSpecification()
        return add(builder.create())
    }

    override fun onExecStart() {
        logger.assertOrThrow(TAG, _routes.isNotEmpty()) {
            "An active route must contain at least one route definition, such as 'idle{}'."
        }
        _routes.forEach {
            it as RouteBase
            it.state = RouteBase.State.IDLE
        }
        if (_active == null) {
            _active = _routes.first() as RouteBase
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
        callOnError?.let {
            execActions.add(ExecAction(context, it))
            callOnError = null
        }

        val route = _active
        route?.collectActions(execActions)
    }
}

