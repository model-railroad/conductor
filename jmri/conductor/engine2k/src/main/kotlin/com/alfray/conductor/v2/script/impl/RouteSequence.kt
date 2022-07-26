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

import com.alflabs.utils.ILogger
import com.alfray.conductor.v2.script.ExecAction
import com.alfray.conductor.v2.script.dsl.IActiveRoute
import com.alfray.conductor.v2.script.dsl.INode
import com.alfray.conductor.v2.script.dsl.IRouteSequence
import com.alfray.conductor.v2.simulator.SimulRouteGraph
import java.util.Locale

/**
 * A route shuttle sequence.
 *
 * This is typically used to implement a circular cyclic shuttle route, e.g. engine going from
 * point A to B and then reversing back from B to A. The route will use several times
 * the same blocks typically in opposite directions with different behaviors. As such, the
 * route is defined as a directed graph where each node represents a block and edges represent
 * transition from one block to the next 'logical' one.
 * There is a main sequence, which is the normal route behavior, then there are branches which
 * allow the route to deviate from the main block sequence, e.g. for alternative routing or
 * for error handling.
 *
 * Routes have one state that matters to the active route:
 * - Idle: the route is not active and not being invoked by the script.
 * - Activated: the route has been activated. Its onActivated callback is called once.
 * - Active: the route is active and processing its normal behavior (e.g. sequence).
 * - Error: the route is in error. Its onRecover callback is called repeatedly.
 *
 * Routes are responsible for identifying their own error state. They do so by calling the
 * active route' reportError() method. This triggers the ActiveRoute's onError callback once,
 * after which the route's onRecover callback is used instead of the normal processing.
 *
 * The onActivated callback can use call start_node() to change the starting node for the route.
 * The starting node is used and validated during the activated-to-active transition. At that
 * point the route also verifies that the starting node is actually an occupied block, and that
 * there are no other occupied blocks on the route.
 * The route manager starts all blocks in either empty or occupied state.
 * (Note: we don't currently allow the route to start with a train crossing a block boundary.
 *  That will result in an error and the route going into recover mode.)
 *
 * In the Active state, the route manager manages blocks and nodes.
 */
internal class RouteSequence(
    override val owner: IActiveRoute,
    logger: ILogger,
    builder: RouteSequenceBuilder
) : RouteBase(logger, owner, builder), IRouteSequence, IRouteManager {
    override val throttle = builder.throttle
    private var startNode: INode? = null
    val timeout = builder.timeout
    val graph = parse(builder.sequence, builder.branches)
    private var currentNode: INode? = null

    override fun toString(): String {
        owner as ActiveRoute
        val index = owner.routeIndex(this)
        val addr = throttle.dccAddress
        return String.format(Locale.US, "Route Sequence #%d (%04d)", index, addr)
    }

    override fun start_node(node: INode) {
        assertOrError(graph.nodes.contains(node)) {
            "ERROR start_node $node is not part of the route $this"
        }
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

    /** Called from ExecEngine2's onExecStart to initialize and validate the state of the route. */
    override fun initRoute() {
        if (currentNode == null) {
            currentNode = startNode ?: graph.start
            assertOrError(currentNode != null) { "ERROR Missing start node for route." }
        }

        var numOccupied = 0
        graph.nodes.forEach { node ->
            node as Node
            val b = node.block as Block
            node.changeState(if (b.active) Block.State.OCCUPIED else Block.State.EMPTY)
            numOccupied++
        }

        assertOrError((currentNode!!.block as Block).state == Block.State.OCCUPIED) {
            "ERROR Route starting yet starting node $currentNode is not occupied."
        }

        assertOrError(numOccupied == 1) {
            "TODO ERROR Route starting with $numOccupied occupied blocks is not supported yet"
            // TODO later we can accept that numOccupied==2 if one is the current node and
            // the other is an adjacent edge, then mark one as trailing.
        }
    }

    /** Invoked by the ExecEngine2 loop _before_ collecting all the actions to evaluate. */
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

        currentNode?.let { node ->
            node as Node
            val block = node.block as Block
            val stillCurrentActive = block.active
            val outgoingNodes = graph.outgoing(node)
            val outgoingActive = outgoingNodes.filter { it.block.active }

            // TBD rewrite this

            // Any other blocks than current or outgoing cannot be active.
            val extraActive = graph.nodes.filter { it !== node && !outgoingNodes.contains(it) }
            assertOrError(extraActive.isEmpty()) {
                "ERROR Unexpected blocks are occupied out of node $node: $extraActive"
            }

            if (outgoingActive.isEmpty()) {
                // It's possible that the currently active block flickers and appears missing for a short
                // period of time.
                assertOrError(stillCurrentActive) {
                    "Current block suddenly became non-active. TBD average/use timer for that."
                }
            } else if (outgoingActive.size == 1) {
                // At that point, we have entered a single new block.

                // TODO update all blocks: trailing-->empty

                // It's also possible that the train is just on 2 blocks -- the current and the next one.
                val enterNode = outgoingActive.first() as Node
                enterNode.changeState(Block.State.OCCUPIED)
                node.changeState(Block.State.TRAILING)

            } else if (outgoingActive.size >= 2) {
                // There can't be more than one block active once engine moves out of the current block
                // thus there can be only either zero or one outgoing node possibilities.
                assertOrError(outgoingActive.size < 2) {
                    "ERROR More than one occupied blocks out of node $node: $outgoingActive"
                }
            }



        }
    }

    /** Invoked by the ExecEngine2 loop to collect all actions to evaluate. */
    override fun collectActions(execActions: MutableList<ExecAction>) {
        when (state) {
            State.ACTIVE -> {
                currentNode?.let {
                    it as Node
                    it.collectActions(execActions)
                }
            }
            else -> {
                super.collectActions(execActions)
            }
        }
    }
}


