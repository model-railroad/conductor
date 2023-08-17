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

/** DSL script interface to build an [ISequenceRoute]. */
interface ISequenceRouteBuilder : IRouteBaseBuilder {
    /** This route. Cannot be null. */
    val route: ISequenceRoute

    /** The throttle controlled by this sequence. Cannot be null. */
    var throttle: IThrottle

    /**
     * The minimum time once a block has been entered before we can reach the next block.
     *
     * It must cover at least the time needed to initially fully enter the block.
     * Any "flaky" sensors in both the occupied and trailing blocks are ignored during that time.
     * The default minimum time for the route's blocks is 10 seconds, unless changed in the
     * route or in its nodes.
     * The minimum time check becomes inactive if set to zero in the route.
     * This check is ignored for the start node of a route.
     */
    var minSecondsOnBlock: Int

    /**
     * The maximum time spent moving on the currently occupied block.
     *
     * Any "flaky" sensor in the occupied block are ignored during that time.
     * The default timeout for the route's blocks is 60 seconds, unless changed in the
     * route or in its nodes.
     * Timeout becomes inactive if set to zero in the route.
     */
    var maxSecondsOnBlock: Int

    /**
     * The maximum time to enter a block, when a train overlaps 2 blocks boundaries.
     *
     * When a train enters a block, wheels can create a contact bridging the trailing block and
     * make it look temporarily occupied. During this timeout, we ignore this effect. After this
     * timeout, it's an error for the trailing block to suddenly become occupied.
     * The default timeout value is 30 seconds, unless changed in the route or in its nodes.
     * Timeout becomes inactive if set to zero in the route.
     */
    var maxSecondsEnterBlock: Int

    /** The non-empty non-null list of nodes for this sequence. */
    var sequence: List<INode>

    /** The possible-empty non-null alternate branches for this sequence. */
    val branches: MutableList<List<INode>>

    /** Creation method to create a new node to be added later to [sequence] or [branches]. */
    fun node(block: IBlock, nodeSpecification: INodeBuilder.() -> Unit) : INode
}

