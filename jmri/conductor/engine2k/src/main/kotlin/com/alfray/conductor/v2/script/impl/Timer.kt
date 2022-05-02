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

import com.alfray.conductor.v2.script.dsl.Delay
import com.alfray.conductor.v2.script.dsl.ITimer

internal class Timer(override val delay: Delay) : ITimer {
    override val name = "@timer@${delay.seconds}"

    var started = false
        private set
    var elapsed : Double = 0.0
        private set

    override val active: Boolean
        get() = elapsed >= delay.seconds

    override fun not(): Boolean = !active

    override fun start() {
        if (!started) {
            started = true
            elapsed = 0.0
        }
    }

    override fun stop() {
        started = false
    }

    override fun reset() {
        started = false
        elapsed = 0.0
    }

    // TODO change to a clock provider with timeElapsed absolute
    fun update(elapsed: Double) {
        // Only increment time if increasing and timer is started.
        // A stopped timer does not update anymore.
        if (started && elapsed >= this.elapsed) {
            this.elapsed = elapsed
        }
    }
}
