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

import com.alfray.conductor.v2.script.ExecContext
import com.alfray.conductor.v2.script.dsl.IActiveRoute
import com.alfray.conductor.v2.script.dsl.INode
import com.alfray.conductor.v2.script.dsl.IRouteSequence
import com.alfray.conductor.v2.script.dsl.TAction
import com.alfray.conductor.v2.simulator.SimulRouteGraph


internal class RouteSequence(
    override val owner: IActiveRoute,
    builder: RouteSequenceBuilder
) : IRouteSequence, IRouteManager {
    override val throttle = builder.throttle
    private var startNode: INode? = null
    val timeout = builder.timeout
    val graph = parse(builder.sequence, builder.branches)
    private val actionOnActivate = builder.actionOnActivate
    private var callOnActivate: TAction? = null
    private var currentNode: INode? = null
    internal val context = object: ExecContext(ExecContext.State.UNKNOWN) {
        override fun onStateChanged(oldState: State, newState: State) {
            TODO("Not yet implemented")
        }
    }

    override fun activate() {
        owner.activate(this)
        callOnActivate = actionOnActivate
    }

    override fun start_node(node: INode) {
        check(graph.nodes.contains(node))
        startNode = node
    }

    private fun parse(sequence: List<INode>, branches: MutableList<List<INode>>): RouteGraph {
        val builder = RouteGraphBuilder()
        builder.setSequence(sequence)
        for (branch in branches) {
            builder.addBranch(branch)
        }

        return builder.build()
    }

    override fun toSimulGraph(): SimulRouteGraph = graph.toSimulGraph()

    override fun manageRoute() {
        // TODO("Not FULLY yet implemented")
        // TBD:
        // import clock, logger, IJmricheck blocks
        // check current block is still active
        // check unexpected blocks are active --> enter error mode
        // need to change block, validate with graph it is as expected
        // deal with decaying block states in the route active->trailing->fee.
        // check flag to know if we nede to call onActivate
        // etc
        if (currentNode == null) {
            currentNode = startNode
            if (currentNode == null) {
                currentNode = graph.start
            }
        }
    }
}


