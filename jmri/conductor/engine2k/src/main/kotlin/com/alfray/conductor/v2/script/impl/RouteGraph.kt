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

import com.alfray.conductor.v2.script.dsl.INode
import com.alfray.conductor.v2.simulator.SimulRouteEdge
import com.alfray.conductor.v2.simulator.SimulRouteGraph

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
    private fun flatten(): List<RouteEdge> {
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

    fun toSimulGraph(): SimulRouteGraph {
        val sStart = start.block.systemName

        val sNodes = nodes.map { n -> n.block.systemName }.distinct()

        val sEdgeMap = mutableMapOf<String, MutableList<SimulRouteEdge>>()
        val sEdges = edges.values
            .flatten()
            .map { gEdge ->
                SimulRouteEdge(
                    gEdge.from.block.systemName,
                    gEdge.to.block.systemName,
                    gEdge.isBranch
                )
            }
            .distinct()
            .forEach { sEdge -> sEdgeMap
                .computeIfAbsent(sEdge.from) { mutableListOf() }
                .add(sEdge) }

        return SimulRouteGraph(sStart, sNodes, sEdgeMap)
    }
}
