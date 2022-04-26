package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.dsl.INode
import com.alfray.conductor.v2.script.dsl.IRoute
import com.alfray.conductor.v2.script.dsl.RouteSequenceBuilder

internal data class GraphNode(
    val node: INode,
    val from: GraphNode?,
    var to: GraphNode?,
    var extraTo: MutableList<GraphNode>? = null
) {
    fun addExtraTo(to: GraphNode) {
        if (extraTo == null) {
            extraTo = mutableListOf()
        }
        extraTo!!.add(to)
    }

    private fun toNodeString(): String = "$node"

    override fun toString(): String {
        val sb = StringBuilder("[")
        if (from != null) {
            sb.append(from.toNodeString()).append(">")
        }
        sb.append(this.toNodeString())
        if (to != null) {
            sb.append(">>").append(to?.toNodeString())
        }
        if (extraTo != null) {
            sb.append(extraTo?.joinToString { "+" + it.toNodeString() })
        }
        sb.append("]")
        return sb.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GraphNode

        if (node != other.node) return false

        return true
    }

    override fun hashCode(): Int {
        return node.hashCode()
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
                    from = last,
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
            val initial = findInGraph(flat, branch.get(0))
            val end = findInGraph(flat, branch.get(sz - 1))
            checkNotNull(initial) { "A branch's first node must be in the sequence graph." }
            checkNotNull(end) { "A branch's end node must be in the sequence graph." }

            var last: GraphNode = initial
            for (index in 1 until sz) {
                val g = GraphNode(
                    node = branch.get(index),
                    from = last,
                    to = null
                )
                if (index == 1) {
                    initial.addExtraTo(g)
                } else {
                    last.to = g
                }
                last = g
            }
            last.to = end
        }

        fun findInGraph(flat: List<GraphNode>, node: INode): GraphNode? {
            return flat.firstOrNull() { it.node == node }
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

//
//    /**
//     * Syntax 1: nodes = [ node1, node2, ..., nodeN ] <br/>
//     * or <br/>
//     * Syntax 2: nodes = [ [ node1, node2, ..., nodeN ], [ array2 ], ...[ arrayN ] ] <br/>
//     */
//    private fun parse(nodes: List<Any>): List<List<INode>> {
//        // TODO create a real graph of nodes here instead of list.
//        val nodes = mutableListOf<List<INode>>()
//
//        // Syntax 1: nodes is an array of SequenceNode (no sub-arrays).
//        val nodeList1 = toNodeList(nodes)
//        if (nodeList1 != null) {
//            nodes.add(nodeList1)
//            return nodes
//        }
//
//        // Syntax 2: nodes is an array of arrays of SequenceNode (only sub-arrays, only 1 level).
//        for (node in nodes) {
//            val nodeList2 = toNodeList(node)
//            if (nodeList2 != null) {
//                nodes.add(nodeList2)
//                continue
//            }
//            // This is not an array of just SequenceNodes, so can't convert to a List.
//            throw IllegalArgumentException(
//                    "Expected [ [ node1, node2 ], [ node3...] ] but got sub-array containing "
//                            + node::class.java.simpleName)
//        }
//        return nodes
//
//        // TBD: route nodes should have more than 1 node in each branch (warning level).
//        // TBD: construct a linked graph, not just a list-of-lists.
//        // TBD: fail on isolated islands in graph.
//        // TBD: pretty-print the graph for debug output.
//    }
//
//    private fun toNodeList(nodes: List<Any>) : List<INode>? {
//        //DEBUG println "toNodeList = ${(nodes instanceof Iterable<?>)}, ${nodes.class} -> $nodes"
//
//        val nodeList = mutableListOf<INode>()
//        for (node in nodes) {
//            if (node is INode) {
//                nodeList.add(node)
//            } else {
//                // This is not an array of just SequenceNodes, so can't convert to a List.
//                return null
//            }
//        }
//        return nodeList
//    }


}
