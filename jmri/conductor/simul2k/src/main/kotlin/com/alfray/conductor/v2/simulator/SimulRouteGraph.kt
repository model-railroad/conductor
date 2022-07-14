package com.alfray.conductor.v2.simulator


data class SimulRouteEdge(val from: String, val to: String, val isBranch: Boolean) {
    /** RouteEdge equality is a strict from-to object equality. */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SimulRouteEdge

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

data class SimulRouteGraph(
    val start: String,
    val nodes: List<String>,
    val edges: Map<String, List<SimulRouteEdge>>
) {
    /**
     * Returns a new [SimulRouteGraph] appending the new graph to the current one.
     * Existing nodes/edges are kept intact.
     */
    fun merge(newGraph: SimulRouteGraph): SimulRouteGraph {
        val newNodes = nodes.plus(newGraph.nodes).distinct()

        val newEdges = mutableMapOf<String, MutableList<SimulRouteEdge>>()
        edges.flatMap { e -> e.value }
            .plus(newGraph.edges.flatMap { e -> e.value })
            .distinct()
            .forEach { newEdge ->
                newEdges.computeIfAbsent(newEdge.from) { mutableListOf() }.add(newEdge)
            }

        return SimulRouteGraph(start, newNodes, newEdges)
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

        var prev : String? = null
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

        return "(start=$start, nodes=$nodes, edges=$sb)"
    }

    fun whereTo(from: String): String? {
        // Get the list of outgoing edges from that block/sensor.
        // The list can be null or empty.
        val dest = edges[from]
        dest?.let {
            // Return the first main-sequence edge we can find, if any.
            val main = dest.firstOrNull { !it.isBranch }
            if (main != null) {
                return main.to
            }
            // Otherwise just return whatever is the first outgoing edge
            return dest.firstOrNull()?.to
        }
        return null
    }
}
