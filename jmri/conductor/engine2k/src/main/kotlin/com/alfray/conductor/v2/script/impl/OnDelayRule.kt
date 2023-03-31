/*
 * Project: Conductor
 * Copyright (C) 2023 alf.labs gmail com,
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

import com.alfray.conductor.v2.script.dsl.Delay
import com.alfray.conductor.v2.script.dsl.TAction
import com.alfray.conductor.v2.script.dsl.TCondition

internal class OnDelayRule(
    condition: TCondition,
    private val delay: Delay,
    private val factory: Factory,
    private val registerTimer: (OnDelayRule) -> Unit,
) : OnRule(condition) {
    private var timer: Timer? = null

    internal val active: Boolean
        get() = timer?.active ?: false

    internal val started: Boolean
        get() = timer?.started ?: false

    internal val durationSec: Int
        get() = delay.seconds

    override fun then(action: TAction) {
        super.then(action)
        registerTimer(this)
    }

    override fun evaluateCondition() : Boolean {
        // If there's no timer, start it when possible.
        if (timer == null) {
            timer = factory.createTimer(delay)
            timer?.start()
            return false
        }

        // If we have a timer, only evaluate the condition once the timer becomes active.
        if (!active) return false

        // Timer is active, now we can evaluate the condition.
        return super.evaluateCondition()
    }
}
