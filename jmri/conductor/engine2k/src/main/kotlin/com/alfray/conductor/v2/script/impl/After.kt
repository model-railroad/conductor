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
import com.alfray.conductor.v2.script.dsl.Delay
import com.alfray.conductor.v2.script.dsl.IAfter
import com.alfray.conductor.v2.script.dsl.IThenAfter
import com.alfray.conductor.v2.script.dsl.TAction
import com.alfray.conductor.v2.utils.assertOrThrow

internal class After(
    val delay: Delay,
    private val parent: After? = null,
    private val registerTimer: (After) -> Unit,
) : IAfter {
    private val TAG = javaClass.simpleName
    private var action: TAction? = null
    private var thenAfter: IAfter? = null
    private var timer: Timer? = null
    internal var invoked: Boolean = false

    internal val active: Boolean
        get() = timer?.active ?: false

    internal val started: Boolean
        get() = timer?.started ?: false

    internal val durationSec: Int
        get() = delay.seconds

    internal val parentDurationSec: Int
        get() = delay.seconds + (parent?.parentDurationSec?:0)

    override fun then(action: TAction) : IThenAfter {
        // We only "register" this After timer when the "then" clause is parsed.
        registerTimer(this)

        val parent = this
        this.action = action
        return object : IThenAfter {
            override fun and_after(delay: Delay): IAfter {
                val after = After(delay, parent, registerTimer)
                thenAfter = after
                return after
            }
        }
    }

    fun eval(logger: ILogger, factory: Factory): TAction? {
        // If there's no timer, start it when possible.
        if (timer == null) {
            // If there's a parent, only continue once it's active.
            if (parent != null && !parent.active) {
                return null
            }
            logger.assertOrThrow(TAG, action != null) {
                "ERROR missing 'then {...}' for 'after' or 'and_after' instruction."
            }
            timer = factory.createTimer(delay)
            timer?.start()
        }

        // If we have a timer, return the action only once when it becomes active.
        if (!invoked && active) {
            invoked = true
            return action!!
        }

        return null
    }
}

