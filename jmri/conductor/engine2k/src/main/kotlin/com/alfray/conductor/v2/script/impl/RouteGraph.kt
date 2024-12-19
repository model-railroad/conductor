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
import com.alfray.conductor.v2.simulator.SimulRouteBlock
import com.alfray.conductor.v2.simulator.SimulRouteEdge
import com.alfray.conductor.v2.simulator.SimulRouteGraph

internal data class RouteGraph(
    /** The first node of the main sequence is the de-facto starting point. */
    val start: INode,
    /** All the nodes in the main sequence and all the optional branches. */
    val nodes: Set<INode>,
    /** All the route edges (node from -> node to), sorted by origin block. */
    val edges: Map<IBlock, List<RouteEdge>>,
    /** All the blocks used by all the nodes. */
    val blocks: Set<IBlock> = nodes.map { it.block }.toSet(),
) {
    /**
     * Returns a flattened view of the graph by visiting all edges from main sequence
     * followed by all edges from all branches in their cycle order. Cycles are omitted.
     * At the end, dump any unreachable edge.
     */
    private fun flatten(): List<RouteEdge> {
        val visited = mutableSetOf<RouteEdge>()
        val toVisit = mutableListOf<RouteEdge>()
        val output = mutableListOf<RouteEdge>()

        // Parse the main sequence
        var nSeq = start
        while (true) {
            val toList = edges[nSeq.block]?.filter { it.from === nSeq }
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
                val toList = edges[nBr.block]?.filter { it.from === nBr }
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

    /**
     * Computes all immediate outgoing nodes out of the provided one.
     *
     * This only returns the set of nodes immediately out of the provided node.
     * To get the set of all nodes beyond the current node, use [forward].
     */
    fun outgoing(node: INode): Set<INode> {
        val nodeEdges = edges[node.block]
        if (nodeEdges == null) return emptySet()
        return nodeEdges.filter { it.from === node }.map { it.to }.toSet()
    }

    /**
     * Computes all the forward (outgoing and beyond) nodes reachable out of the provided one.
     *
     * To get the set of nodes immediately after the current node, use [outgoing].
     */
    fun forward(node: INode): Set<INode> {
        val fwd = mutableSetOf<INode>()
        val visit = mutableSetOf(node)

        while (visit.isNotEmpty()) {
            val n = visit.first()
            visit.remove(n)
            outgoing(n).forEach {
                if (fwd.add(it)) {
                    // Only visit if newly added to the fwd set
                    visit.add(it)
                }
            }
        }

        return fwd.toSet()
    }

    override fun toString(): String {
        val sb = StringBuilder()

        val flatEdges = flatten()

        var prev : INode? = null
        for (edge in flatEdges) {
            if (prev == null) {
                sb.append("[" + edge.from)
            } else if (prev !== edge.from) {
                sb.append("],[" + edge.from)
            }
            sb.append(if (edge.isBranch) '-' else '=')
                .append(if (edge.forward) '>' else '<')
                .append('>')
            sb.append(edge.to)
            prev = edge.to
        }
        if (prev != null) {
            sb.append("]")
        }

        return sb.toString()
    }

    fun toSimulGraph(): SimulRouteGraph {
        val sBlocks = nodes
            .distinctBy { n -> n.block.systemName }
            .map { n -> (n.block as INodeBlock).toSimulRouteBlock((n as Node).reversal) }
        val sBlocksMap = mutableMapOf<String, SimulRouteBlock>()
        sBlocks.forEach { sBlocksMap[it.systemName] = it }

        val sStart = sBlocksMap[start.block.systemName]!!

        val sEdgeMap = mutableMapOf<SimulRouteBlock, MutableList<SimulRouteEdge>>()
        edges.values
            .flatten()
            .map { gEdge ->
                SimulRouteEdge(
                    sBlocksMap[gEdge.from.block.systemName]!!,
                    sBlocksMap[gEdge.to.block.systemName]!!,
                    gEdge.forward,
                    gEdge.isBranch,
                )
            }
            .distinct()
            .forEach { sEdge -> sEdgeMap
                .computeIfAbsent(sEdge.from) { mutableListOf() }
                .add(sEdge) }

        return SimulRouteGraph(sStart, sBlocks, sEdgeMap)
    }
}
