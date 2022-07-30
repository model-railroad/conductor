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

internal class RouteGraphBuilder(private val logger: ILogger) {
    private val TAG = javaClass.simpleName
    private lateinit var start : INode
    private val nodes = mutableSetOf<INode>()
    private val edges = mutableListOf<RouteEdge>()

    /** Must be called once to set the main block sequence. */
    fun setSequence(sequence: List<INode>): RouteGraphBuilder {
        // It's fine for a sequence to contain a single node and no edges, but it cannot be empty.
        logger.assertOrThrow(TAG, sequence.isNotEmpty()) {
            "A route sequence cannot be empty, it must have at least one node."
        }

        // The first node of the sequence is the default starting point of the graph.
        start = sequence.first()
        nodes.add(start)
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
            nodes.add(n)
            computeReversal(prev = lastLastN, current = lastN, next = n)
            addEdge(from = lastN, to = n, isBranch = false)
            lastLastN = lastN
            lastN = n
        }
        computeReversal(prev = lastLastN, current = lastN, next = null)

        return this
    }

    private fun computeReversal(prev: INode?, current: INode, next: INode?) {
        current as Node
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
        logger.assertOrThrow(TAG, nodes.contains(branch.first())) {
            "A branch's first node must already be in the sequence graph: $branch"
        }
        logger.assertOrThrow(TAG, nodes.contains(branch.last())) {
            "A branch's end node must already be in the sequence graph: $branch"
        }

        // Add all nodes if not already present.
        // Create all branch edges if not already defined.
        var lastLastN: INode? = null
        var lastN : INode = branch.first()
        for (n in branch.subList(1, branch.size)) {
            nodes.add(n)
            computeReversal(prev = lastLastN, current = lastN, next = n)
            addEdge(from = lastN, to = n, isBranch = true)
            lastLastN = lastN
            lastN = n
        }

        return this
    }

    private fun addEdge(from: INode, to: INode, isBranch: Boolean) {
        logger.assertOrThrow(TAG, from !== to) {
            "A route node cannot lead to itself: $from"
        }
        val edge = RouteEdge(from, to, isBranch)
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

        return RouteGraph(start, nodes, edgeMap)
    }
}
