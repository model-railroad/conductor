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
import com.alfray.conductor.v2.script.dsl.IBlock
import com.alfray.conductor.v2.script.dsl.INode
import com.alfray.conductor.v2.utils.assertOrThrow
import dagger.assisted.AssistedInject

internal class RouteGraphBuilder @AssistedInject constructor(
        private val logger: ILogger
) {
    private val TAG = javaClass.simpleName
    private lateinit var start : INode
    /** Map of nodes to the direction leading to this node. */
    private val nodes = mutableMapOf<INode, Boolean>()
    /** Ordered list of graph edges. */
    private val edges = mutableListOf<RouteEdge>()

    /** Must be called once to set the main block sequence. */
    fun setSequence(sequence: List<INode>): RouteGraphBuilder {
        // It's fine for a sequence to contain a single node and no edges, but it cannot be empty.
        logger.assertOrThrow(TAG, sequence.isNotEmpty()) {
            "A sequence route cannot be empty, it must have at least one node."
        }

        // The first node of the sequence is the default starting point of the graph.
        start = sequence.first()

        // The start node is by definition not a reversal node.
        // It is de facto in the "forward" direction.
        var forward = true
        nodes.put(start, forward)
        start.let {
            it as Node
            if (it.reversal == null) {
                it.reversal = false
            }
        }

        // Add all nodes if not already present.
        // Create all sequence edges if not already defined.
        var lastLastN: INode? = null
        var lastN = start
        for (n in sequence.subList(1, sequence.size)) {
            if (computeReversal(prev = lastLastN, current = lastN, next = n)) {
                forward = !forward
            }
            addEdge(from = lastN, to = n, forward = forward, isBranch = false)
            nodes.putIfAbsent(n, forward)
            lastLastN = lastN
            lastN = n
        }
        computeReversal(prev = lastLastN, current = lastN, next = null)

        return this
    }

    private fun computeReversal(prev: INode?, current: INode, next: INode?): Boolean {
        current as Node
        // Set the value if not already defined.
        if (current.reversal == null) {
            // The previous node was a reversal node if its leading and outgoing *blocks*
            // are the same (e.g. B1 => B2 => B1). Note that even though the reversal flag
            // is stored in the node, it is the block equality that matters.
            if (prev != null && next != null) {
                current.reversal = (prev.block == next.block)
            } else {
                current.reversal = false
            }
        }
        // Return the value we would have computed rather than the one already stored.
        return prev != null && next != null && prev.block == next.block
    }

    /** Can be called zero or more times to create branches off previousky added nodes. */
    fun addBranch(branch: List<INode>): RouteGraphBuilder {
        logger.assertOrThrow(TAG, ::start.isInitialized) {
            "A sequence must be defined before its branches."
        }
        logger.assertOrThrow(TAG, branch.size >= 2) {
            "A branch must have at least 2 nodes (start -> [ branch ] -> end): $branch"
        }

        // Initial and end nodes must be in the graph already
        val first = branch.first()
        logger.assertOrThrow(TAG, nodes.containsKey(first)) {
            "A branch's first node must already be in the sequence graph: $branch"
        }
        logger.assertOrThrow(TAG, nodes.containsKey(branch.last())) {
            "A branch's end node must already be in the sequence graph: $branch"
        }

        var forward = nodes[first]!!

        // Add all nodes if not already present.
        // Create all branch edges if not already defined.
        var lastLastN: INode? = null
        var lastN : INode = first
        for (n in branch.subList(1, branch.size)) {
            if (computeReversal(prev = lastLastN, current = lastN, next = n)) {
                forward = !forward
            }
            addEdge(from = lastN, to = n, forward = forward, isBranch = true)
            nodes.putIfAbsent(n, forward)
            lastLastN = lastN
            lastN = n
        }

        return this
    }

    private fun addEdge(from: INode, to: INode, forward: Boolean, isBranch: Boolean) {
        logger.assertOrThrow(TAG, from !== to) {
            "A route node cannot lead to itself: $from"
        }
        val edge = RouteEdge(from, to, forward, isBranch)
        if (!edges.contains(edge)) {
            edges.add(edge)
        }
    }

    /**
     * Called after all the calls to [setSequence] and [addBranch] have been completed
     * to compute the final [RouteGraph].
     */
    fun build() : RouteGraph {
        logger.assertOrThrow(TAG, ::start.isInitialized) {
            "A sequence must be defined for the route."
        }

        val edgeMap = mutableMapOf<IBlock, MutableList<RouteEdge>>()

        edges.forEach { edge -> edgeMap
            .computeIfAbsent(edge.from.block) { mutableListOf() }
            .add(edge) }

        return RouteGraph(start, nodes.keys, edgeMap)
    }
}
