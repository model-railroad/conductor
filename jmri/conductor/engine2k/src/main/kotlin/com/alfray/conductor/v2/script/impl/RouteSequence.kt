package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.dsl.INode
import com.alfray.conductor.v2.script.dsl.IRoute
import com.alfray.conductor.v2.script.dsl.RouteSequenceBuilder

internal data class GraphNode(
    val node: INode,
    var to: GraphNode?,
    var extraTo: MutableList<GraphNode>? = null
) {
    fun addTo(to: GraphNode) {
        if (this.to == null) {
            this.to = to
            return
        }
        if (extraTo == null) {
            extraTo = mutableListOf()
        }
        extraTo!!.add(to)
    }

    private fun toNodeString(): String = "$node"

    override fun toString(): String {
        val sb = StringBuilder("[")
        sb.append(this.toNodeString())
        if (to != null) {
            sb.append("->").append(to?.toNodeString())
        } else {
            sb.append("<>")
        }
        if (extraTo != null) {
            sb.append(extraTo?.joinToString(
                separator = "+", prefix = "+") {
                it.toNodeString() })
        }
        sb.append("]")
        return sb.toString()
    }
}

internal class RouteSequence(builder: RouteSequenceBuilder) : IRoute {
    val throttle = builder.throttle
    val timeout = builder.timeout
    val startNode = parse(builder.sequence, builder.branches)
    val actionOnActivate = builder.actionOnActivate

    private fun parse(sequence: List<INode>, branches: MutableList<List<INode>>): GraphNode {
        val start = sequenceToLinearGraph(sequence)
        for (branch in branches) {
            addGraphBranch(start, branch)
        }
        return start
    }

    companion object {
        fun sequenceToLinearGraph(sequence: List<INode>): GraphNode {
            check(sequence.isNotEmpty())
            var start: GraphNode? = null
            var last: GraphNode? = null

            for (n in sequence) {
                val g = GraphNode(
                    node = n,
                    to = null
                )

                if (start == null) {
                    start = g
                }
                if (last != null) {
                    last.to = g
                }
                last = g
            }

            return start!!
        }

        fun addGraphBranch(start: GraphNode, branch: List<INode>) {
            val sz = branch.size
            check(sz >= 2) { "A branch must have at least 2 nodes (start -> branch -> end)" }

            // Initial and end nodes must be in the graph already
            val flat = visitGraph(start)
            val initial = findInGraph(flat, branch[0])
            val end = findInGraph(flat, branch[sz - 1])
            checkNotNull(initial) { "A branch's first node must be in the sequence graph." }
            checkNotNull(end) { "A branch's end node must be in the sequence graph." }

            var last: GraphNode = initial
            var index = 1
            while (index < sz-1) {
                val g = GraphNode(
                    node = branch[index],
                    to = null
                )
                last.addTo(g)
                last = g
                index++
            }
            last.addTo(end)
        }

        fun findInGraph(flat: List<GraphNode>, node: INode): GraphNode? {
            return flat.firstOrNull { it.node == node }
        }

        /** Returns a flat view of the graph, with sequences first. */
        fun visitGraph(
            start: GraphNode,
            visited: MutableSet<GraphNode> = mutableSetOf()
        ): List<GraphNode> {
            val list = mutableListOf<GraphNode>()
            // Visit main sequence first
            var g: GraphNode? = start
            while (g != null && g !in visited) {
                list.add(g)
                visited.add(g)
                g = g.to
            }
            // Visit branches later
            var g2: GraphNode? = start
            while (g2 != null) {
                g2.extraTo?.let {
                    for (br in it) {
                        if (br !in visited) {
                            val brList = visitGraph(br, visited)
                            if (brList.isNotEmpty()) {
                                list.addAll(brList)
                            }
                        }
                    }
                }
                g2 = g2.to
            }
            return list
        }

        fun printGraph(start: GraphNode) : String =
            visitGraph(start).joinToString(" -> ")
    }
}
