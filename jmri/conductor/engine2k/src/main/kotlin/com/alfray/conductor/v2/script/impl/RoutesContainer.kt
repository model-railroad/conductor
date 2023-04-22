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

import com.alflabs.kv.IKeyValue
import com.alflabs.manifest.Prefix
import com.alflabs.manifest.RouteInfo
import com.alflabs.utils.IClock
import com.alflabs.utils.ILogger
import com.alfray.conductor.v2.script.ExecAction
import com.alfray.conductor.v2.script.ExecContext
import com.alfray.conductor.v2.script.dsl.IRoutesContainer
import com.alfray.conductor.v2.script.dsl.IRoute
import com.alfray.conductor.v2.script.dsl.IIdleRouteBuilder
import com.alfray.conductor.v2.script.dsl.IOnRule
import com.alfray.conductor.v2.script.dsl.ISequenceRoute
import com.alfray.conductor.v2.script.dsl.ISequenceRouteBuilder
import com.alfray.conductor.v2.script.dsl.TAction
import com.alfray.conductor.v2.simulator.ISimulCallback
import com.alfray.conductor.v2.utils.assertOrThrow
import java.util.Locale


/**
 * A routes container is a group of routes, of which one and only one is active at a given time.
 *
 * A routes container must have at least one route associated with it. Routes can have different
 * characteristics -- e.g. an 'idle' route does absolutely nothing, whereas a 'sequence' route
 * implements a shuttle. Furthermore, the 'idle' route exists with the sole purpose of having a
 * default no-op route when a routes container must not be operating (since a routes container must
 * have an 'active' route, per definition).
 *
 * Routes have one state that matters to the routes' container:
 * - Idle: the route is not active and not being invoked by the script.
 * - Activated: the route has been activated. Its onActivated callback is called once.
 * - Active: the route is active and processing its normal behavior (e.g. sequence).
 * - Error: the route is in error.
 *
 * Routes are responsible for identifying their own error state. They do so by calling the
 * routes container [reportError] method. This triggers the current Route's onError callback once
 * followed by calling the RoutesContainer's onError callback once.
 */
internal class RoutesContainer(
    private val clock: IClock,
    private val logger: ILogger,
    private val keyValue: IKeyValue,
    private val simulCallback: ISimulCallback?,
    builder: RoutesContainerBuilder
) : IRoutesContainer, IExecEngine {
    private val TAG = javaClass.simpleName
    override val name = builder.name
    override val toggle = builder.toggle
    override val status = builder.status
    private val actionOnError = builder.actionOnError
    private var callOnError: TAction? = null
    private var _active: RouteBase? = null
    private val _routes = mutableListOf<IRoute>()
    private val context = ExecContext(ExecContext.Reason.ROUTE_CONTAINER)
    var routeInfo: RouteInfo = createRouteInfo()
        private set

    override val active: IRoute
        get() = _active!!
    override val routes: List<IRoute>
        get() = _routes
    override val error: Boolean
        get() = _active?.state == RouteBase.State.ERROR

    /** Helper returning the index of this route in the routes container table.
     * Used for pretty-printing the route toString. Returns -1 if route unknown. */
    fun routeIndex(route: IRoute): Int = routes.indexOf(route)

    /** A short display of the routes container name.
     * For detailed information suitable for logging purposes, use [getLogStatus]. */
    override fun toString(): String {
        return "RoutesContainer $name"
    }

    override fun getLogStatus(): String {
        val sb = StringBuilder()
        val index = if (_active == null) -1 else routeIndex(_active!!)

        sb.append(_active?.javaClass?.simpleName?.replace("Route", ""))
            .append(" #$index = ")
            .append(if (toggle.active) "ON" else "OFF")
            .append(' ')
            .append(_active?.state)
            .append(' ')
            .append(status.invoke())

        val countTimers = context.countTimers()
        _active?.let { r ->
            countTimers.add(r.context.countTimers())
            if (r is SequenceRoute) {
                r.currentNode?.let {
                    it as Node
                    countTimers.add(it.eventContext.countTimers())
                }
            }
        }
        sb.append(", ${countTimers.numTimers} timers")
        if (countTimers.numTimers > 0) {
            sb.append(" (${countTimers.numStarted} started, ${countTimers.numActive} active, ${countTimers.durationSec} sec)")
        }

        return sb.toString()
    }

    /**
     * Called by script to change the active route.
     * If the route is already active, it gets reset and re-activated.
     */
    override fun activate(route: IRoute) {
        logger.assertOrThrow(TAG, route in _routes) {
            "ERROR $this: cannot activate a route not part of a routes container: $route"
        }

        _active?.let {
            // If a new route is being activated:
            //      --> the old route goes to IDLE, new route goes to ACTIVATED.
            // If the same route is being re-activated:
            //      --> it goes from current state (IDLE, ERROR, ACTIVE) to ACTIVATED below.
            it.changeState(RouteBase.State.IDLE)
        }

        _active = route as RouteBase
        _active?.changeState(RouteBase.State.ACTIVATED)

        simulCallback?.let {
            if (route is SequenceRoute) {
                val simulGraph = route.toSimulGraph()
                simulCallback.setRoute(route.throttle.dccAddress, route.timeout, simulGraph)
            }
        }
    }

    /** Called by routes to indicate their error state has changed. */
    fun reportError(route: IRoute, isError: Boolean) {
        if (route === _active) {
            logger.d(TAG, "Routes Container $name: $route reports error $isError")
            if (isError) {
                callOnError = actionOnError
                route.changeState(RouteBase.State.ERROR)
            } else {
                callOnError = null
                route.changeState(RouteBase.State.ACTIVATED)
            }
        } else {
            logger.d(TAG, "Routes Container $name: Ignore non-active $route reports error $isError")
        }
    }

    private fun add(route: IRoute): IRoute {
        logger.assertOrThrow(TAG, route !in _routes) {
            "ERROR $this: trying to add route already part of a routes container: $route"
        }
        _routes.add(route)
        return route
    }

    override fun idle(idleRouteSpecification: IIdleRouteBuilder.() -> Unit): IRoute {
        val builder = IdleRouteBuilder(this, logger)
        builder.idleRouteSpecification()
        return add(builder.create())
    }

    override fun sequence(sequenceRouteSpecification: ISequenceRouteBuilder.() -> Unit): IRoute {
        val builder = SequenceRouteBuilder(this, clock, logger)
        builder.sequenceRouteSpecification()
        return add(builder.create())
    }

    override fun onExecStart() {
        logger.assertOrThrow(TAG, _routes.isNotEmpty()) {
            "ERROR $this: A routes container must contain at least one route definition, such as 'idle{}'."
        }
        _routes.forEach {
            it as RouteBase
            it.changeState(RouteBase.State.IDLE)
        }
        if (_active == null) {
            activate(_routes.first())
        }
        val route = _active
        if (route is IRouteManager) {
            route.initRouteManager()
        }
    }

    /** Invoked by the ExecEngine2 loop _before_ collecting all the actions to evaluate. */
    override fun onExecHandle() {
        val route = _active
        if (route is IRouteManager) {
            route.manageRoute()
        }
        exportRouteInfo()
    }

    /** Invoked by the ExecEngine2 loop to collect all actions to evaluate. */
    fun collectActions(
        execActions: MutableList<ExecAction>,
        collectOnRuleAction: (ExecContext, IOnRule) -> Unit
    ) {
        context.evalOnRules(collectOnRuleAction)

        _active?.collectActions(execActions, collectOnRuleAction)

        callOnError?.let {
            execActions.add(ExecAction(context, it))
            callOnError = null
        }
    }

    private fun createRouteInfo(): RouteInfo {
        // Sanitize the name into a key, hopefully unique.
        val key = name
            .trim()
            .ifEmpty { this.hashCode().toString() }
            .lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]+"), "_")
        val prefix = Prefix.Route

        return RouteInfo(
            name,
            "$prefix$key\$toggle",
            "$prefix$key\$status",
            "$prefix$key\$counter",
            "$prefix$key\$throttle"
        )
    }

    private fun exportRouteInfo() {
        val route = _active

        (toggle as Sensor).export(routeInfo.toggleKey)

        if (route is ISequenceRoute) {
            (route.throttle as Throttle).export(routeInfo.throttleKey)
        } else {
            keyValue.putValue(routeInfo.throttleKey, "0", true /*broadcast*/)
        }

        route?.let {
            val counter = it.activationCounter.toString()
            keyValue.putValue(routeInfo.counterKey, counter, true /*broadcast*/)
        }

        val statusText = status.invoke()
        keyValue.putValue(routeInfo.statusKey, statusText, true /*broadcast*/)
    }
}

