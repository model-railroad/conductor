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
import com.alfray.conductor.v2.script.dsl.IRoutesContainer
import com.alfray.conductor.v2.script.dsl.INode
import com.alfray.conductor.v2.script.dsl.IRouteIdle

/**
 * An idle route.
 *
 * Used to provide a no-op route to an [IRoutesContainer] when no trains should be running.
 * That's because a routes container should always have a current active route.
 *
 * An idle route has an onActivated callback invoked when the route is first invoked.
 *
 * The idle route also inherits the base behavior of having an onRecover callback to
 * deal with the route error state, however since an idle route does nothing it is not
 * expected that it would ever enter error & recovery mode.
 *
 * The base start_node() method does not apply to an idle route and will throw an exception
 * if used.
 */
internal class RouteIdle(
    owner: IRoutesContainer,
    logger: ILogger,
    builder: RouteIdleBuilder
) : RouteBase(logger, owner, builder), IRouteIdle {
    private val TAG = javaClass.simpleName
    private val actionOnIdle = builder.actionOnIdle

    override fun start_node(node: INode) {
        assertOrError(false) {
            "No start_node to set in an idle route."
        }
    }

    override fun toString(): String {
        owner as RoutesContainer
        val index = owner.routeIndex(this)
        return "Route Idle ${owner.name}#$index"
    }

    /** Invoked by the ExecEngine2 loop to collect all actions to evaluate. */
    override fun collectActions(execActions: MutableList<ExecAction>) {
        when (state) {
            State.ACTIVE -> {
                actionOnIdle?.let {
                    execActions.add(ExecAction(context, it))
                }
            }
            else -> {
                super.collectActions(execActions)
            }
        }
    }
}
