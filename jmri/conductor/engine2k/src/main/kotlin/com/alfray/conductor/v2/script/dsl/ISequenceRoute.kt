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

@file:Suppress("FunctionName")

package com.alfray.conductor.v2.script.dsl

import com.alfray.conductor.v2.simulator.SimulRouteGraph

/** DSL script interface for a sequence route. */
interface ISequenceRoute : IRoute {
    /** The [IThrottle] operating on this route. */
    val throttle: IThrottle

    /** The [INode] main sequence provided to the sequence route builder. */
    val sequence: List<INode>

    /** Max time in seconds that a running train can take to cross an active block.
     * Timeout becomes inactive if set to zero. */
    val maxSecondsOnBlock: Int

    /** Internal Converts the route graph into a Simulator route graph. */
    fun toSimulGraph(): SimulRouteGraph
    // TODO ISequenceRoute.toSimulGraph should not be exposed in the DSL.
}
