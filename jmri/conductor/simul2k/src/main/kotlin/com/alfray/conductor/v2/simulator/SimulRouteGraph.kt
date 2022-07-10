package com.alfray.conductor.v2.simulator


internal data class SimulRouteEdge(val from: String, val to: String, val isBranch: Boolean) {
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

internal data class SimulRouteGraph(
    val start: String,
    val nodes: Set<String>,
    val edges: Map<String, List<SimulRouteEdge>>
)
