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

import com.alfray.conductor.v2.script.dsl.IActiveRoute
import com.alfray.conductor.v2.script.dsl.INode
import com.alfray.conductor.v2.script.dsl.IRoute
import com.alfray.conductor.v2.script.dsl.RouteSequenceBuilder


internal class RouteSequence(
    override val owner: IActiveRoute,
    builder: RouteSequenceBuilder) : IRoute {
    private var startNode: INode? = null
    val throttle = builder.throttle
    val timeout = builder.timeout
    val graph = parse(builder.sequence, builder.branches)
    val actionOnActivate = builder.actionOnActivate

    override fun activate() {
        owner.activate(this)
    }

    override fun start_node(node: INode) {
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
}

internal data class RouteEdge(val from: INode, val to: INode, val isBranch: Boolean) {
    /** RouteEdge equality is a strict from-to object equality. */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RouteEdge

        // Must use === and not == here. We want to compare pointers, not content.
        if (from !== other.from) return false
        if (to !== other.to) return false
        // The "isBranch" type is NOT part of the equality test. Two branches are
        // still equal even if they differ only on their branch type.
        return true
    }

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + isBranch.hashCode()
        return result
    }
}


internal data class RouteGraph(
    val start: INode,
    val nodes: Set<INode>,
    val edges: Map<INode, List<RouteEdge>>
) {
    /**
     * Returns a flattened view of the graph by visiting all edges from main sequence
     * followed by all edges from all branches in their cycle order. Cycles are omitted.
     * At the end, dump any unreachable edge.
     */
    fun flatten(): List<RouteEdge> {
        val visited = mutableSetOf<RouteEdge>()
        val toVisit = mutableListOf<RouteEdge>()
        val output = mutableListOf<RouteEdge>()

        // Parse the main sequence
        var nSeq = start
        while (true) {
            val toList = edges[nSeq]
            if (toList == null || toList.isEmpty()) {
                break
            }

            val edgeList = toList.filter { !it.isBranch && !visited.contains(it) }
            if (edgeList.isEmpty()) {
                break
            }
            val edge = edgeList.first()
            visited.add(edge)

            output.add(edge)
            toVisit.addAll(toList.subList(1, toList.size))
            nSeq = edge.to
        }

        // Parse all branches forked from the main sequence, and queue any new branch we find.
        while (toVisit.isNotEmpty()) {
            val visit = toVisit.removeAt(0)
            var nBr = visit.from
            while (true) {
                val toList = edges[nBr]
                if (toList == null || toList.isEmpty()) {
                    break
                }

                val edgeList = toList.filter { !visited.contains(it) }
                if (edgeList.isEmpty()) {
                    break
                }
                val edge = edgeList.first()
                visited.add(edge)
                toVisit.remove(edge)

                output.add(edge)
                toVisit.addAll(toList.subList(1, toList.size))
                nBr = edge.to
            }
        }

        // Finally dump any edge that has not been reached by the main sequence or its
        // forked branches. Ideally there should not be any.
        output.addAll(edges.values.flatten().subtract(visited))

        return output
    }

    override fun toString(): String {
        val sb = StringBuilder()

        val edges = flatten()

        var prev : INode? = null
        for (edge in edges) {
            if (prev == null) {
                sb.append("[" + edge.from)
            } else if (prev !== edge.from) {
                sb.append("],[" + edge.from)
            }
            sb.append(if (edge.isBranch) "->" else "=>")
            sb.append(edge.to)
            prev = edge.to
        }
        if (prev != null) {
            sb.append("]")
        }

        return sb.toString()
    }
}

internal class RouteGraphBuilder {
    private lateinit var start : INode
    private val nodes = mutableSetOf<INode>()
    private val edges = mutableListOf<RouteEdge>()

    fun build() : RouteGraph {
        check(::start.isInitialized) { "A sequence must be defined for the route." }

        val edgeMap = mutableMapOf<INode, MutableList<RouteEdge>>()

        edges.forEach { edge -> edgeMap
            .computeIfAbsent(edge.from) { mutableListOf() }
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
