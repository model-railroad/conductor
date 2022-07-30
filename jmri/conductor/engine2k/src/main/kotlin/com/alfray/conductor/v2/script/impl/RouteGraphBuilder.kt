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

import com.alfray.conductor.v2.script.dsl.IBlock
import com.alfray.conductor.v2.script.dsl.INode

internal class RouteGraphBuilder {
    private lateinit var start : INode
    private val nodes = mutableSetOf<INode>()
    private val edges = mutableListOf<RouteEdge>()

    fun build() : RouteGraph {
        check(::start.isInitialized) { "A sequence must be defined for the route." }

        val edgeMap = mutableMapOf<IBlock, MutableList<RouteEdge>>()

        edges.forEach { edge -> edgeMap
            .computeIfAbsent(edge.from.block) { mutableListOf() }
            .add(edge) }

        return RouteGraph(start, nodes, edgeMap)
    }

    fun setSequence(sequence: List<INode>): RouteGraphBuilder {
        // It's fine for a sequence to contain a single node and no edges, but it cannot be empty.
        check(sequence.isNotEmpty()) { "A route sequence cannot be empty." }

        // The first node of the sequence is the default starting point of the graph.
        start = sequence.first()

        // Add all nodes if not already present.
        // Create all sequence edges if not already defined.
        var lastN = start
        for (n in sequence) {
            nodes.add(n)
            if (n !== lastN) {
                addEdge(lastN, n, isBranch = false)
            }
            lastN = n
        }

        return this
    }

    fun addBranch(branch: List<INode>): RouteGraphBuilder {
        check(::start.isInitialized) { "A sequence must be defined before its branches." }
        check(branch.size >= 2) { "A branch must have at least 2 nodes (start -> branch -> end)" }

        // Initial and end nodes must be in the graph already
        check(nodes.contains(branch.first())) { "A branch's first node must be in the sequence graph." }
        check(nodes.contains(branch.last())) { "A branch's end node must be in the sequence graph." }

        // Add all nodes if not already present.
        // Create all branch edges if not already defined.
        var lastN : INode = branch.first()
        for (n in branch) {
            nodes.add(n)
            if (n !== lastN) {
                addEdge(lastN, n, isBranch = true)
            }
            lastN = n
        }

        return this
    }

    private fun addEdge(from: INode, to: INode, isBranch: Boolean) {
        check(from !== to) { "A route node cannot lead to itself." }
        val edge = RouteEdge(from, to, isBranch)
        if (!edges.contains(edge)) {
            edges.add(edge)
        }
    }
}
