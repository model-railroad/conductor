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

package com.alfray.conductor.v2.simulator

import kotlin.math.max

/**
 * A block for the simulator.
 * 'systemName' is the internal unique identifier for the block,
 * whereas 'name' is an option script-provided name for display purposes.
 */
data class SimulRouteBlock(
    val systemName: String,
    val name: String,
    val virtual: Boolean,
    val reversal: Boolean,
) {
    var extraTimersSec: Int = 0
        private set

    override fun toString(): String {
        return if (reversal) {
            "<$name>"
        } else {
            "{$name}"
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SimulRouteBlock

        if (systemName != other.systemName) return false
        return true
    }

    override fun hashCode(): Int {
        return systemName.hashCode()
    }

    fun updateNodeTimers(sumTimersSec: Int) {
        extraTimersSec = max(extraTimersSec, sumTimersSec)
    }
}
