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
import com.alfray.conductor.v2.script.dsl.INode
import com.alfray.conductor.v2.script.dsl.IRoute
import com.alfray.conductor.v2.utils.ConductorExecException

/**
 */
internal abstract class RouteBase(
    protected val logger: ILogger,
    override val owner: IActiveRoute,
    builder: RouteBaseBuilder
) : IRoute {
    private val TAG = javaClass.simpleName
    private val actionOnActivate = builder.actionOnActivate
    private val actionOnRecover = builder.actionOnRecover
    protected val context = ExecContext(ExecContext.State.ROUTE)
    var activationCounter = 0
        private set

    internal enum class State {
        IDLE,
        ACTIVATED,
        ACTIVE,
        ERROR
    }

    /** The state of this route, as managed by the [IActiveRoute] implementation. */
    var state = State.IDLE
        set(value) {
            if (field != value) {
                if (field == State.ACTIVATED && value == State.ACTIVE) {
                    // keep ACTIVATED timers when going to the ACTIVE state.
                } else {
                    // Clear all context timers.
                    context.afterTimers.clear()
                }
                // Update state
                field = value
                logger.d(TAG, "$this is now $state")
            }
        }

    /** Internal utility that routes derived implementation can use to set the route in error mode. */
    protected inline fun assertOrError(value: Boolean, lazyMessage: () -> Any) {
        if (!value) {
            (owner as ActiveRoute).reportError(this, true)
            val message = lazyMessage()
            throw ConductorExecException(message.toString())
        }
    }

    /** Invoked by script to activate this route in its active route owner. */
    override fun activate() {
        owner.activate(this)
    }

    /** Invoked by script to change the start_node during the onActivated callback. */
    abstract override fun start_node(node: INode)

    /** Invoked by the ExecEngine2 loop to collect all actions to evaluate. */
    open fun collectActions(execActions: MutableList<ExecAction>) {
        when (state) {
            State.ERROR -> {
                actionOnRecover?.let {
                    execActions.add(ExecAction(context, it))
                }
            }
            State.ACTIVATED -> {
                activationCounter++
                actionOnActivate?.let {
                    execActions.add(ExecAction(context, it))
                }
                state = State.ACTIVE
            }
            State.ACTIVE -> {
                // no-op ... typically overridden by derived class.
            }
            State.IDLE -> {
                // no-op
            }
        }
    }
}


