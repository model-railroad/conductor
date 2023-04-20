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

import com.alflabs.utils.IClock
import com.alflabs.utils.ILogger
import com.alfray.conductor.v2.script.ExecAction
import com.alfray.conductor.v2.script.dsl.IRoutesContainer
import com.alfray.conductor.v2.script.dsl.IBlock
import com.alfray.conductor.v2.script.dsl.INode
import com.alfray.conductor.v2.script.dsl.ISequenceRoute
import com.alfray.conductor.v2.script.dsl.TAction
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
 * Routes have one state that matters to the routes' container:
 * - Idle: the route is not active and not being invoked by the script.
 * - Activated: the route has been activated. Its onActivated callback is called once.
 * - Active: the route is active and processing its normal behavior (e.g. sequence).
 * - Error: the route is in error. Its onRecover callback is called repeatedly.
 *
 * Routes are responsible for identifying their own error state. They do so by calling the
 * routes' container reportError() method. This triggers the RoutesContainer's onError callback
 * once, after which the route's onRecover callback is used instead of the normal processing.
 *
 * The onActivated callback can call startNode() to change the starting node for the route.
 * The starting node is used and validated during the activated-to-active transition. At that
 * point the route also verifies that the starting node is actually an occupied block, and that
 * there are no other occupied blocks on the route.
 * The route manager starts all blocks in either empty or occupied state.
 * (Note: we don't currently allow the route to start with a train crossing a block boundary.
 *  That will result in an error and the route going into recover mode.)
 *
 * In the Active state, the route manager manages blocks and nodes.
 */
internal class SequenceRoute(
    override val owner: IRoutesContainer,
    private val clock: IClock,
    logger: ILogger,
    builder: SequenceRouteBuilder
) : RouteBase(logger, owner, builder), ISequenceRoute, IRouteManager {
    private val TAG = javaClass.simpleName
    private var currentActiveBlocks = setOf<IBlock> ()
    override val throttle = builder.throttle
    override val sequence = builder.sequence
    private var startNode: INode? = null
    private var blockStartMS = 0L
    override val timeout = builder.timeout
    val graph = parse(builder.sequence, builder.branches)
    var currentNode: INode? = null
        private set

    private fun timeoutExpired(): Boolean =
        blockStartMS > 0 &&
                timeout > 0 &&
                ((clock.elapsedRealtime() - blockStartMS) > 1000L * timeout)

    private fun clearTimeout() {
        blockStartMS = 0
    }

    private fun startTimeout() {
        blockStartMS = clock.elapsedRealtime()
    }

    override fun toString(): String {
        owner as RoutesContainer
        val index = owner.routeIndex(this)
        val addr = throttle.dccAddress
        return String.format(Locale.US, "SequenceRoute %s#%d (%04d)", owner.name, index, addr)
    }

    override fun startNode(node: INode) {
        assertOrError(graph.nodes.contains(node)) {
            "ERROR startNode($node) is not part of the route $this"
        }
        startNode = node
    }

    private fun parse(sequence: List<INode>, branches: MutableList<List<INode>>): RouteGraph {
        val builder = RouteGraphBuilder(logger)
        builder.setSequence(sequence)
        for (branch in branches) {
            builder.addBranch(branch)
        }

        return builder.build()
    }

    override fun toSimulGraph(): SimulRouteGraph = graph.toSimulGraph()

    /** Called from ExecEngine2's onExecStart to initialize and validate the state of the route. */
    override fun initRouteManager() {
    }

    override fun changeState(newState: State) {
        super.changeState(newState)

        when (newState) {
            State.IDLE -> {
                onSequenceRouteIdle()
            }
            State.ACTIVATED -> {
                // no-op. postOnActivateAction() will be executed after [actionOnActivate].
            }
            State.ACTIVE -> {}
            State.ERROR -> {}
        }
    }

    /**
     * Called _after_ the route's [actionOnActivate] has completed,
     * when this sequence route becomes activated.
     *
     * This gave the script a chance to change to startNode, thus at this point we can
     * validate that the start node is both defined and occupied, and that no other block
     * is occupied.
     */
    override fun postOnActivateAction() {
        logger.d(TAG, "@@ DEBUG postOnActivateAction $this // start node is $currentNode")

        // Set or reset the initial start node.
        currentNode = startNode ?: graph.start
        logger.d(TAG, "$this start node is $currentNode")
        assertOrError(currentNode != null) { "ERROR Missing start node for $this." }
        startTimeout()

        // Set every block to its initial state of either occupied or empty.
        val currentNode_ = currentNode as Node
        val currentBlock = currentNode_.block
        var currentBlockIsOccupied = false
        val otherBlockOccupied = mutableSetOf<IBlock>()
        graph.nodes.forEach { node ->
            node as Node
            val b = node.block
            node.changeState(if (b.active) IBlock.State.OCCUPIED else IBlock.State.EMPTY)
            if (b.active) {
                if (b == currentBlock) {
                    currentBlockIsOccupied = b.active
                } else {
                    otherBlockOccupied.add(node.block)
                }
            }
        }

        // Validate the route's initial state.
        // At the minimum, the initial start node should actually be occupied.
        // We do not need to validate whether any other block is occupied as that is going
        // to be done by the next [manageRoute] call to the manager.
        assertOrError(currentBlockIsOccupied) {
            "ERROR $this cannot start because start node $currentNode is not occupied."
        }

        logger.d(TAG, "@@ DEBUG onSequenceRouteActivated schedule onEnter for $currentNode_")
        // Ensure that the onEnter callback of the currently occupied block is executed
        // since activating a route is akin to entering the current/starting block.
        currentNode_.changeEnterState()
    }

    /** Called by [changeState] when this sequence route ends and becomes idle. */
    private fun onSequenceRouteIdle() {
        // Remove any trailing blocks. We don't need them as they will not be
        // updated by this route manager anymore.
        graph.nodes.forEach { node ->
            node as Node
            val b = node.block
            if (b.state == IBlock.State.TRAILING) {
                node.changeState(IBlock.State.EMPTY)
                logger.d(TAG, "[idle] block $b becomes ${b.state}")
            }
        }
    }

    /** Invoked by the ExecEngine2 loop _before_ collecting all the actions to evaluate. */
    override fun manageRoute() {
        // Expected block change behavior:
        // Block 1 is occupied
        // Case A: Train still on same block. Block 1 active, no other active.
        // Case B: Train moves to block 2 ⇒ block 2 sensor is active, block 1 is not active.
        // Case C: Train moves to block 2 ⇒ block 2 sensor is active, block 1 keeps active.
        // Any block that becomes active which is not an outgoing node is a potential error.

        val allActiveBlocks = graph.blocks.filterTo(mutableSetOf()) { it.active }
        val newActiveBlocks = allActiveBlocks.minus(currentActiveBlocks)
        currentActiveBlocks = allActiveBlocks

        currentNode?.let { node ->
            node as Node
            val block = node.block

            val stillCurrentActive = block.active
            val outgoingNodes = graph.outgoing(node)
            // A suitable outgoing node can only be a *newly* active (transitioning from non-active
            // to active) block.
            var outgoingNodesActive = outgoingNodes.filter { newActiveBlocks.contains(it.block) }
            val trailingBlocks = graph.blocks
                .filter { it.state == IBlock.State.TRAILING }
                .toSet()

            // Any other blocks other than current, trailing, or outgoing cannot be active.
            // Note that we really want to match blocks here, not nodes.
            val extraBlocksActive = allActiveBlocks
                .minus(node.block)
                .minus(trailingBlocks)
                .minus(outgoingNodes.map { it.block }.toSet())
            assertOrError(extraBlocksActive.isEmpty()) {
                "ERROR $this has unexpected occupied blocks out of $node: $extraBlocksActive"
            }

            // Virtual Blocks Management
            // If the current block has become inactive, and the next block is virtual,
            // can we activate it?
            if (!stillCurrentActive && outgoingNodes.size == 1) {
                val vblock = outgoingNodes.first().block
                if (vblock is VirtualBlock) {
                    // The single outgoing virtual block can become active if we do not have
                    // any active blocks: not the current one, not any of the outgoing ones,
                    // and not even the currently trailing one.
                    if (outgoingNodesActive.isEmpty()
                        && trailingBlocks.intersect(allActiveBlocks).isEmpty()) {
                        // Since there's only one unambiguous outgoing virtual block, activate it.
                        logger.d(TAG, "Virtual block activated $vblock")
                        vblock.active(true)
                        outgoingNodesActive = listOf(outgoingNodes.first())
                    }
                }
            }

            // All Blocks Management
            if (outgoingNodesActive.isEmpty()) {
                // Case A: Train still on same block. Current block active, no other active.
                assertOrError(stillCurrentActive || !timeoutExpired()) {
                    "ERROR $this current block suddenly became non-active after $timeout seconds."
                }
            } else if (outgoingNodesActive.size >= 2) {
                // The train cannot exit to more than one outgoing edge, so this has to be an error.
                assertOrError(outgoingNodesActive.size < 2) {
                    "ERROR $this has more than one occupied blocks out of $node: $outgoingNodesActive"
                }
            } else if (outgoingNodesActive.size == 1) {
                // At that point, we have entered a single new block.
                // The current node can either become inactive or remain inactive (aka trailing).
                // Any trailing block becomes empty, current occupied becomes trailing.

                graph.nodes
                    .filter { (it.block as INodeBlock).state == IBlock.State.TRAILING }
                    .forEach { n ->
                        n as Node
                        n.changeState(IBlock.State.EMPTY)
                        logger.d(TAG, "trailing block $n becomes ${n.block.state}")
                    }

                // Mark current block as trailing.
                node.changeState(IBlock.State.TRAILING)
                logger.d(TAG, "current block ${node.block} becomes ${node.block.state}")
                if (node.block is VirtualBlock) {
                    logger.d(TAG, "Trailing Virtual Block deactivated ${node.block}")
                    node.block.active(false)
                }

                // Mark the outgoing block as the new occupied block.
                val enterNode = outgoingNodesActive.first() as Node
                enterNode.changeState(IBlock.State.OCCUPIED)
                logger.d(TAG, "enter block ${enterNode.block} becomes ${enterNode.block.state}")
                currentNode = enterNode
                clearTimeout()
            }
        }

        // Handle current block timeout
        if (currentNode == null) {
            // Ignore timeout if we have no current block or if the train is stopped.
            clearTimeout()
        } else if (throttle.stopped) {
            clearTimeout()
        } else {
            // We have a moving train on an active node.
            if (blockStartMS == 0L) {
                startTimeout()
            }
            assertOrError(!timeoutExpired()) {
                "ERROR $this current block timeout expired after $timeout seconds."
            }
        }
    }

    /** Invoked by the ExecEngine2 loop to collect all actions to evaluate. */
    override fun collectActions(execActions: MutableList<ExecAction>) {
        when (state) {
            State.ACTIVATED -> {
                super.collectActions(execActions)
            }
            State.ACTIVE -> {
                currentNode?.let {
                    it as Node
                    it.collectActions(execActions)
                }
            }
            else -> {
                clearTimeout()
                super.collectActions(execActions)
            }
        }
    }
}


