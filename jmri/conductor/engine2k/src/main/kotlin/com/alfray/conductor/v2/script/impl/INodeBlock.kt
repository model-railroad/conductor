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

import com.alfray.conductor.v2.script.dsl.IBlock
import com.alfray.conductor.v2.simulator.SimulRouteBlock

/**
 * TBD
 */
internal interface INodeBlock  {
    /** The cached active property of the underlying sensor. Updated in [onExecHandle] only. */
    val active: Boolean

    /** Occupancy state of the block: empty, occupied, or trailing. */
    val state : IBlock.State

    /** Internal method used by a Node to change the Block occupancy state. */
    fun changeState(newState: IBlock.State)

    fun toSimulRouteBlock(reversal: Boolean?): SimulRouteBlock
}
