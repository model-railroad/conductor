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

import com.alflabs.conductor.util.EventLogger
import com.alflabs.utils.ILogger
import com.alfray.conductor.v2.script.ExecAction
import com.alfray.conductor.v2.script.ExecContext
import com.alfray.conductor.v2.script.dsl.IRoutesContainer
import com.alfray.conductor.v2.script.dsl.INode
import com.alfray.conductor.v2.script.dsl.IRoute
import com.alfray.conductor.v2.script.dsl.IIdleRoute
import com.alfray.conductor.v2.script.dsl.TAction
import com.alfray.conductor.v2.utils.assertOrThrow

internal abstract class RouteBase(
    protected val logger: ILogger,
    protected val eventLogger: EventLogger,
    override val owner: IRoutesContainer,
    builder: RouteBaseBuilder
) : IRoute {
    private val TAG = javaClass.simpleName
    private val actionOnActivate = builder.actionOnActivate
    private val actionOnError = builder.actionOnError
    private var callOnError: TAction? = null
    override val name = builder.name
    val context = ExecContext(ExecContext.Reason.ROUTE)
    var activationCounter = 0
        private set

    internal enum class State {
        /** A route that is currently not being used (aka "inactive") */
        IDLE,
        /** A route that is in the process of being activated. Will change to [ACTIVE] next. */
        ACTIVATED,
        /** The currently active route. */
        ACTIVE,
        /** A route that is in error/recovery mode. */
        ERROR
    }

    /** The state of this route, as managed by the [IRoutesContainer] implementation. */
    var state = State.IDLE
        private set

    /** Internal setter to change the state of this route. */
    open fun changeState(newState: State) {
        val oldState = state
        if (oldState != newState) {
            when {
                oldState == State.ACTIVATED && newState == State.ACTIVE -> {
                    // keep ACTIVATED timers when going to the ACTIVE state.
                }
                newState == State.ERROR -> {
                    callOnError = actionOnError
                    context.clearTimers()
                }
                else -> {
                    // Clear all context timers.
                    context.clearTimers()
                }
            }
            // Update state
            state = newState
            eventLogger.logAsync(EventLogger.Type.Route, toString(), state.name)
        }
    }

    /**
     * Internal utility that routes implementations can use to set the route in error mode.
     * This does NOT throw an exception, unlike [ILogger.assertOrThrow].
     */
    protected inline fun assertOrError(value: Boolean, lazyMessage: () -> Any) {
        if (!value) {
            val message = lazyMessage().toString()
            eventLogger.logAsync(EventLogger.Type.Route, toString(), message)
            (owner as RoutesContainer).reportError(this, true)
        }
    }

    /** Invoked by script to activate this route in its routes container owner. */
    override fun activate() {
        owner.activate(this)
    }

    /** Invoked by script to change the start_node during the onActivated callback. */
    abstract override fun startNode(startNode: INode, trailing: INode?)

    /** Invoked by the ExecEngine2 loop to collect all actions to evaluate. */
    open fun collectActions(execActions: MutableList<ExecAction>) {
        when (state) {
            State.ERROR -> {
                callOnError?.let {
                    execActions.add(ExecAction(context, context, it))
                    callOnError = null
                }
            }
            State.ACTIVATED -> {
                if (this !is IIdleRoute) {
                    // Count the number of route activations, except for idle routes
                    // since there's no "running" during an idle route.
                    activationCounter++
                }
                execActions.add(ExecAction(context, context) {
                    actionOnActivate?.invoke()
                    postOnActivateAction()
                    // actionOnActivate or postOnActivateAction may have set the route in ERROR.
                    // Only move to ACTIVE state if the route is still in ACTIVATED state.
                    if (state == State.ACTIVATED) {
                        changeState(State.ACTIVE)
                    }
                })
            }
            State.ACTIVE -> {
                // no-op ... typically overridden by derived class.
            }
            State.IDLE -> {
                // no-op
            }
        }
    }

    /**
     * Called _after_ the route's [actionOnActivate] has completed,
     * when this sequence route becomes activated.
     * Start node is known at this point.
     */
    open fun postOnActivateAction() {}

}


