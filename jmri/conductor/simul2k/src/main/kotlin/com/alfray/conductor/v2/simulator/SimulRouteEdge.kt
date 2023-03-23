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

/**
 * An edge in the simulated route. The edge is a directed edge from one block to another one.
 * The 'forward' direction corresponds to the initial shuttle starting direction (which may or
 * may not match a DCC engine 'forward' direction.)
 */
data class SimulRouteEdge(
    val from: SimulRouteBlock,
    val to: SimulRouteBlock,
    val forward: Boolean,
    val isBranch: Boolean,
) {
    /** RouteEdge equality is a strict from-to object equality,
     * 'forward' and 'isBranch' is not used. */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SimulRouteEdge

        if (from != other.from) return false
        if (to != other.to) return false
        // The "isBranch" type is NOT part of the equality test. Two branches are
        // still equal even if they differ only on their forward direction or branch type.
        return true
    }

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 31 * result + to.hashCode()
        return result
    }
}
