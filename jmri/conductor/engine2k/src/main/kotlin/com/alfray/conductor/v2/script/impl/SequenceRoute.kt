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

@file:Suppress("LocalVariableName")

package com.alfray.conductor.v2.script.impl

import com.alflabs.conductor.util.EventLogger
import com.alflabs.utils.IClock
import com.alflabs.utils.ILogger
import com.alfray.conductor.v2.script.ExecAction
import com.alfray.conductor.v2.script.dsl.IBlock
import com.alfray.conductor.v2.script.dsl.INode
import com.alfray.conductor.v2.script.dsl.IRoutesContainer
import com.alfray.conductor.v2.script.dsl.ISequenceRoute
import com.alfray.conductor.v2.simulator.SimulRouteGraph
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
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
 * - Error: the route is in error.
 *
 * Routes are responsible for identifying their own error state. They do so by calling the
 * routes' container reportError() method. This triggers the current Route's onError callback once
 *  * followed by calling the RoutesContainer's onError callback once.
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
internal class SequenceRoute @AssistedInject constructor(
        private val clock: IClock,
        logger: ILogger,
        private val factory: Factory,
        eventLogger: EventLogger,
        @Assisted override val owner: IRoutesContainer,
        @Assisted builder: SequenceRouteBuilder
) : RouteBase(logger, eventLogger, owner, builder), ISequenceRoute, IRouteManager {
    private val TAG = javaClass.simpleName
    private var currentActiveBlocks = setOf<IBlock> ()
    override val throttle = builder.throttle
    override val sequence = builder.sequence
    private var occupiedStartNode: INode? = null
    private var trailingStartNode: INode? = null
    override val minSecondsOnBlock = builder.minSecondsOnBlock
    override val maxSecondsOnBlock = builder.maxSecondsOnBlock
    override val maxSecondsEnterBlock = builder.maxSecondsEnterBlock
    val graph = parse(builder.sequence, builder.branches)
    var currentNode: INode? = null
        private set

    companion object {
        private data class Timeout(val timeout: Int, val elapsed: Double) {
            val enabled: Boolean = timeout > 0
            val reached: Boolean = enabled && elapsed >= timeout
        }
    }

    /** Returns the [minSecondsOnBlock] for the [currentNode] or the current route. */
    private fun computeMinSecondsOnBlock(node: Node): Int =
        if (node.minSecondsOnBlock > 0) node.minSecondsOnBlock else minSecondsOnBlock

    /** Computes the [Timeout] for [minSecondsOnBlock] time. */
    private fun minSecondsReached(node: Node): Timeout {
        val timeoutSeconds = computeMinSecondsOnBlock(node)
        val block = node.block as BlockBase
        val stateTimeSeconds = block.stateTimeMs() / 1000.0
        return Timeout(timeoutSeconds, stateTimeSeconds)
    }

    /** Returns the [maxSecondsOnBlock] for the given [node] or the current route. */
    private fun computeMaxSecondsOnBlock(node: Node): Int =
        if (node.maxSecondsOnBlock > 0) node.maxSecondsOnBlock else maxSecondsOnBlock

    /** Computes the [Timeout] for [maxSecondsOnBlock] time. */
    private fun maxSecondsOnBlockReached(node: Node): Timeout {
        val timeoutSeconds = computeMaxSecondsOnBlock(node)
        val block = node.block as BlockBase
        val stateTimeSeconds = block.stateTimeMs() / 1000.0
        return Timeout(timeoutSeconds, stateTimeSeconds)
    }

    /** Returns the [maxSecondsEnterBlock] for the given [node] or the current route. */
    private fun computeMaxSecondsEnterBlock(node: Node): Int =
        if (node.maxSecondsEnterBlock > 0) node.maxSecondsEnterBlock else maxSecondsEnterBlock

    /** Computes the [Timeout] for [maxSecondsEnterBlock] time. */
    private fun maxSecondsEnterBlockReached(node: Node): Timeout {
        val timeoutSeconds = computeMaxSecondsEnterBlock(node)
        val block = node.block as BlockBase
        val stateTimeSeconds = block.stateTimeMs() / 1000.0
        return Timeout(timeoutSeconds, stateTimeSeconds)
    }

    override fun toString(): String {
        owner as RoutesContainer
        val index = owner.routeIndex(this)
        val addr = throttle.dccAddress
        val name_ = if (name.isEmpty()) "" else "$name "
        return String.format(Locale.US, "Sequence %s #%d %s(%04d)", owner.name, index, name_, addr)
    }

    @Suppress("UnnecessaryVariable")
    override fun startNode(startNode: INode, trailing: INode?) {
        val occupiedStartNode = startNode
        val trailingStartNode = trailing
        assertOrError(graph.nodes.contains(occupiedStartNode)) {
            "ERROR occupied start node ($occupiedStartNode) is not part of the route $this"
        }
        trailingStartNode?.let {
            assertOrError(graph.nodes.contains(trailingStartNode)) {
                "ERROR trailing start node ($trailingStartNode) is not part of the route $this"
            }
            assertOrError(graph.outgoing(trailingStartNode).contains(occupiedStartNode)) {
                "ERROR occupied start node ($occupiedStartNode) is not an outgoing node for ($trailingStartNode) in route $this"
            }
        }
        this.occupiedStartNode = occupiedStartNode
        this.trailingStartNode = trailingStartNode
        logger.d(TAG, buildString {
            append("Start node set to $occupiedStartNode")
            trailingStartNode?.let { append(" w/ trailing $trailingStartNode") }
        })
    }

    private fun parse(sequence: List<INode>, branches: MutableList<List<INode>>): RouteGraph {
        val builder = factory.createRouteGraphBuilder()
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
                onSequenceRouteActivated()
                // postOnActivateAction() will be executed after [actionOnActivate].
            }
            State.ACTIVE -> {}
            State.ERROR -> {}
        }
    }

    /**
     * Called as soon as this sequence route becomes activated,
     * and before the route's [actionOnActivate] is invoked.
     * Start node is not known yet.
     */
    private fun onSequenceRouteActivated() {

        // Force the blocks' state timer to reset by setting all blocks to EMPTY
        // and then set them back to TRAILING / OCCUPIED as needed (done in postOnActivateAction).
        graph.nodes.forEach { node ->
            node as Node
            node.changeState(IBlock.State.EMPTY)
        }
    }

    /**
     * Called _after_ the route's [actionOnActivate] has completed,
     * when this sequence route becomes activated.
     * Start node is known at this point.
     *
     * This gives the script a chance to change to startNode, thus at this point we can
     * validate that the start node is both defined and occupied, and that no other block
     * is occupied.
     */
    override fun postOnActivateAction() {
        // Set or reset the initial start node.
        currentNode = occupiedStartNode ?: graph.start
        logger.d(TAG, "$this start node is $currentNode")
        assertOrError(currentNode != null) { "ERROR Missing start node for $this." }

        // Set every block to its initial state of either occupied or empty.
        val currentNode_ = currentNode as Node
        val currentBlock = currentNode_.block

        val trailingStartBlock = trailingStartNode?.block

        var currentBlockIsOccupied = false
        val otherBlockOccupied = mutableSetOf<IBlock>()

        graph.nodes.forEach { node ->
            node as Node
            val b = node.block
            node.changeState(
                when {
                    b.active && b == trailingStartBlock -> IBlock.State.TRAILING
                    b.active -> IBlock.State.OCCUPIED
                    else -> IBlock.State.EMPTY
                }
            )
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

        currentNode?.let { currentNode_ ->
            currentNode_ as Node
            val currentBlock = currentNode_.block

            val stillCurrentActive = currentBlock.active
            val outgoingNodes = graph.outgoing(currentNode_)
            // A suitable outgoing node can only be a *newly* active (transitioning from non-active
            // to active) block.
            var outgoingNodesActive = outgoingNodes.filter { newActiveBlocks.contains(it.block) }
            val trailingBlocks = graph.blocks
                .filter { it.state == IBlock.State.TRAILING }
                .toSet()

            // Any other blocks other than current, trailing, or outgoing cannot be active.
            // Note that we really want to match blocks here, not nodes.
            val extraBlocksActive = allActiveBlocks
                .minus(currentNode_.block)
                .minus(trailingBlocks)
                .minus(outgoingNodes.map { it.block }.toSet())
            assertOrError(extraBlocksActive.isEmpty()) {
                "ERROR $this has unexpected occupied blocks out of $currentNode_: $extraBlocksActive"
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
                //
                // Assert that the current block sensor is still active.
                // However, since a train can occupy a block for up to maxSecondsOnBlock, we accept
                // that the sensor may be flaky and could seem temporarily off when we read it here.
                val onBlockTimeout = maxSecondsOnBlockReached(currentNode_)
                assertOrError(
                        stillCurrentActive ||
                        !(onBlockTimeout.enabled && onBlockTimeout.reached)) {
                    val elapsed = String.format("%.1f", onBlockTimeout.elapsed)
                    val timeout = String.format("%.1f", onBlockTimeout.timeout)
                    "ERROR $this current block ${currentNode_.block} suddenly became non-active after $elapsed seconds " +
                            "(max occupancy is $timeout seconds)."
                }
            } else if (outgoingNodesActive.size >= 2) {
                // The train cannot exit to more than one outgoing edge, so this has to be an error.
                assertOrError(outgoingNodesActive.size < 2) {
                    "ERROR $this has more than one occupied blocks out of $currentNode_: $outgoingNodesActive"
                }
            } else if (outgoingNodesActive.size == 1) {
                // At that point, we have entered a single new block.
                val enterNode = outgoingNodesActive.first() as Node

                // Did the next block activate too soon?
                val enterBlockTimeout = maxSecondsEnterBlockReached(currentNode_)
                val ignoreTrailing =
                        (enterBlockTimeout.enabled && !enterBlockTimeout.reached) &&
                        (trailingBlocks.contains(enterNode.block) || currentNode_ === occupiedStartNode)
                if (ignoreTrailing) {
                    // If this is a reversing shuttle scenario (the trailing block is also the outgoing block)
                    // then we need to ignore trailing block activations during the first maxSecondsEnterBlockElapsed
                    // seconds since the entering train may be activating its own trailing block.
                    //
                    // We also need to ignore the minSecondsOnBlock check below on the start node,
                    // since the train may be at the edge of the block and exit it right away.
                    val elapsed = String.format("%.1f", enterBlockTimeout.elapsed)
                    logger.d(TAG, "WARNING ignore trailing block $enterNode activated after $elapsed seconds.")
                } else {
                    // We should have been on the current node for at least minSecondsOnBlock.
                    val minSecondsTimeout = minSecondsReached(currentNode_)
                    assertOrError(!minSecondsTimeout.enabled || minSecondsTimeout.reached) {
                        val timeout = minSecondsTimeout.timeout
                        val elapsed = String.format("%.1f", minSecondsTimeout.elapsed)
                        "ERROR $this next block ${enterNode.block} activated in $elapsed seconds. " +
                        "Current block ${currentNode_.block} must remain occupied for at least $timeout seconds."
                    }

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
                    // Any timer for min/maxSecondsOnBlock becomes reset after the state change.
                    currentNode_.changeState(IBlock.State.TRAILING)
                    if (currentNode_.block is VirtualBlock) {
                        logger.d(TAG, "Trailing Virtual Block deactivated ${currentNode_.block}")
                        currentNode_.block.active(false)
                    }

                    // Mark the outgoing block as the new occupied block.
                    enterNode.changeState(IBlock.State.OCCUPIED)
                    currentNode = enterNode
                }
            }
        }

        // Handle current block timeout
        if (currentNode == null) {
            // Ignore max timeout if we have no current block.
            // No-op.
        } else if (currentNode!!.block.occupied) {
            // We have a train on an occupied node.

            // This block cannot be occupied for more than the maxSecondsOnBlock time limit.
            val currentNode_ = currentNode as Node
            val secondsOnBlockTimeout = maxSecondsOnBlockReached(currentNode_)
            assertOrError(!(secondsOnBlockTimeout.enabled && secondsOnBlockTimeout.reached)) {
                val elapsed = String.format("%.1f", secondsOnBlockTimeout.elapsed)
                "ERROR $this current block ${currentNode_.block} still occupied after $elapsed seconds."
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
                super.collectActions(execActions)
            }
        }
    }
}


