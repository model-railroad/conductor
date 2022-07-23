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

import com.alfray.conductor.v2.script.ExecAction
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
    private val context = ExecContext(ExecContext.State.ROUTE)
    var error = false
        private set(v) {
            field = v
            (owner as ActiveRoute).reportError(this, v)
        }

    inline fun checkError(value: Boolean, lazyMessage: () -> Any) {
        if (!value) {
            error = true
            val message = lazyMessage()
            throw IllegalStateException(message.toString())
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

    /** Called from ExecEngine2's onExecStart to initialize and validate the state of the route. */
    override fun initRoute() {
        // TBD this may be too early? Happens onExecStart --> when loading the script.
        // In tests, we don't have access to sensors/blocks to active them yet so this
        // will ALWAYS fail with "currnet occupied block not active".
        if (currentNode == null) {
            currentNode = startNode ?: graph.start
            checkError(currentNode != null) { "ERROR Missing start node for route." }
        }

        var numOccupied = 0
        graph.nodes.forEach { node ->
            val b = node.block as Block
            b.changeState(if (b.active) Block.State.OCCUPIED else Block.State.EMPTY)
            numOccupied++
        }

        checkError((currentNode!!.block as Block).state == Block.State.OCCUPIED) {
            "ERROR Route starting yet starting node $currentNode is not occupied."
        }

        checkError(numOccupied == 1) {
            "TODO ERROR Route starting with more than 1 occupied block is not supported yet"
            // TODO later we can accept that numOccupied==2 if one is the current node and
            // the other is an adjacent edge, then mark one as trailing.
        }

        error = false
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

            // Any other blocks than current or outgoing cannot be active.
            val extraActive = graph.nodes.filter { it !== node && !outgoingNodes.contains(it) }
            checkError(extraActive.isEmpty()) {
                "ERROR Unexpected blocks are occupied out of node $node: $extraActive"
            }

            if (outgoingActive.isEmpty()) {
                // It's possible that the currently active block flickers and appears missing for a short
                // period of time.
                checkError(stillCurrentActive) {
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
                checkError(outgoingActive.size < 2) {
                    "ERROR More than one occupied blocks out of node $node: $outgoingActive"
                }
            }



        }
    }

    /** Invoked by the ExecEngine2 loop to collect all actions to evaluate. */
    fun collectActions(execActions: MutableList<ExecAction>) {
        callOnActivate?.let {
            execActions.add(ExecAction(context, it))
            callOnActivate = null
        }
        currentNode?.let {
            it as Node
            it.collectActions(execActions)
        }
    }
}


