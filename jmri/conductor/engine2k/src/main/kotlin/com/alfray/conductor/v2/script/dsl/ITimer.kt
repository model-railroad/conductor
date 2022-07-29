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

package com.alfray.conductor.v2.script.dsl

data class Delay(val seconds: Int)

val Int.seconds: Delay
    get() = Delay(this)

/** DSL script interface for a global timer.
 * <p/>
 * Timers are initialized with a specific duration. Scripts only start or reset the timer.
 * A timer is active once it has reached its expiration time and remains active until it
 * is either restarted or reset.
 */
interface ITimer: IActive {
    /** Computed name of the timer. TBD switch to IVarName. */
    val name: String

    /** Duration of that timer, in seconds. */
    val delay: Delay

    /** Starts the timer. Does nothing if the timer has already expired and is active. */
    fun start()

    /** Resets and stop the timer. */
    fun reset()
}
