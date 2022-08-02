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

import com.alfray.conductor.v2.script.impl.After


/**
 * ExecEngine scoped context for on..then rules and after..then timers.
 * There's one context per route or node and its state, including a global one.
 * There's always one current context active at a given time, which depends on which
 * route/node callback is being executed.
 * The context for route or node gets cleared when the object's state changes.
 */
internal open class ExecContext(private val reason: Reason) {
    private val afterTimers_ = mutableListOf<After>()
    val afterTimers: List<After> = afterTimers_

    enum class Reason {
        LOAD_SCRIPT,
        GLOBAL_RULE,
        ON_RULE,
        ACTIVE_ROUTE,
        ROUTE,
        NODE,
    }

    fun clearTimers() {
        afterTimers_.clear()
    }

    fun addTimer(after: After) {
        afterTimers_.add(after)
    }

    /** Returns a summary representation of the timers' activity for log purposes:
     *  Count: num timers, num started timer, num active timers. */
    fun countTimers(): CountTimers {
        val t = afterTimers_.size
        val s = if (t == 0) 0 else afterTimers_.count { it.started }
        val a = if (t == 0) 0 else afterTimers_.count { it.active }
        return CountTimers(t, s, a)
    }

    data class CountTimers(var numTimers: Int, var numStarted: Int, var numActive: Int) {
        fun add(other: CountTimers): CountTimers {
            numTimers += other.numTimers
            numStarted += other.numStarted
            numActive += other.numActive
            return this
        }
    }
}
