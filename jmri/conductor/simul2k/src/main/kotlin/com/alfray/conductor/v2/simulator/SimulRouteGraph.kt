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

package com.alfray.conductor.v2.simulator

/**
 * A route graph used in the simulation.
 */
data class SimulRouteGraph(
    val start: SimulRouteBlock,
    val blocks: List<SimulRouteBlock>,
    val edges: Map<SimulRouteBlock, List<SimulRouteEdge>>
) {
    /**
     * Returns a new [SimulRouteGraph] appending the new graph to the current one.
     * Existing blocks/edges are kept intact.
     */
    fun merge(newGraph: SimulRouteGraph): SimulRouteGraph {
        val newBlocks = blocks.plus(newGraph.blocks).distinct()

        val newEdges = mutableMapOf<SimulRouteBlock, MutableList<SimulRouteEdge>>()
        edges.flatMap { e -> e.value }
            .plus(newGraph.edges.flatMap { e -> e.value })
            .distinct()
            .forEach { newEdge ->
                newEdges.computeIfAbsent(newEdge.from) { mutableListOf() }.add(newEdge)
            }

        return SimulRouteGraph(start, newBlocks, newEdges)
    }

    /**
     * Returns a flattened view of the graph by visiting all edges from main sequence
     * followed by all edges from all branches in their cycle order. Cycles are omitted.
     * At the end, dump any unreachable edge.
     */
    private fun flatten(): List<SimulRouteEdge> {
        val visited = mutableSetOf<SimulRouteEdge>()
        val toVisit = mutableListOf<SimulRouteEdge>()
        val output = mutableListOf<SimulRouteEdge>()

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

        var prev : SimulRouteBlock? = null
        for (edge in edges) {
            if (prev == null) {
                sb.append("[" + edge.from)
            } else if (prev != edge.from) {
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

        return "(start=$start, blocks=$blocks, edges=$sb)"
    }

    fun whereTo(from: SimulRouteBlock, dirForward: Boolean): SimulRouteBlock? {
        // Get the list of outgoing edges from that block/sensor.
        // The list can be null or empty.
        val dest = edges[from]

        // Filter by direction. We should not choose nodes in the wrong direction.
        val filtered = dest?.filter { it.forward == dirForward }

        // Sort the list by by main-to-branch.
        // Note: compareBy{} sorts booleans in order (false, true).
        val result = filtered?.sortedWith(compareBy { it.isBranch })

        // Pick the first choice from the selected list, if any.
        return result?.firstOrNull()?.to
    }
}
