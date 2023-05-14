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

    /**
     * The minimum time once a block has been entered before we can reach the next block.
     *
     * It must cover at least the time needed to initially fully enter the block.
     * Any "flaky" sensors in both the occupied and trailing blocks are ignored during that time.
     * The default minimum time for the route's blocks is 10 seconds, unless changed in the
     * route or in its nodes.
     * The minimum time check becomes inactive if set to zero in the route.
     */
    val minSecondsOnBlock: Int

    /**
     * The maximum time spent moving on the currently occupied block.
     *
     * Any "flaky" sensor in the occupied block are ignored during that time.
     * The default timeout for the route's blocks is 60 seconds, unless changed in the
     * route or in its nodes.
     * Timeout is reset when the train stops.
     * Timeout becomes inactive if set to zero in the route.
     */
    val maxSecondsOnBlock: Int

    /** Internal Converts the route graph into a Simulator route graph. */
    fun toSimulGraph(): SimulRouteGraph
    // TODO ISequenceRoute.toSimulGraph should not be exposed in the DSL.
}
