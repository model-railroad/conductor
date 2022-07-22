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

package com.alfray.conductor.v2.script

import com.alfray.conductor.v2.script.impl.Timer


/**
 * ExecEngine scoped context for on..then rules and after..then timers.
 * There's one context per route or node and its state, including a global one.
 * There's always one current context active at a given time, which depends on which
 * route/node callback is being executed.
 * The context for route or node gets cleared when the object's state changes.
 */
internal abstract class ExecContext(private var state: State) {
    val afterTimers = mutableListOf<Timer>()

    enum class State {
        UNKNOWN,
        GLOBAL,
        ROUTE_ACTIVATE,
        ROUTE_ERROR,
        NODE_ENTER,
        NODE_OCCUPIED,
        NODE_TRAILING,
        NODE_EMPTY,
    }

    abstract fun onStateChanged(oldState: State, newState: State)

    fun changeState(newState: State) {
        if (newState != state) {
            val oldState = state
            state = newState
            onStateChanged(oldState, newState)
        }
    }

}
