package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.dsl.INode
import com.alfray.conductor.v2.script.dsl.IRoute
import com.alfray.conductor.v2.script.dsl.RouteSequenceBuilder

internal class RouteSequence(builder: RouteSequenceBuilder) : IRoute {
    val throttle = builder.throttle
    val timeout = builder.timeout
    val nodes: List<List<INode>> = parse(builder.nodes)
    var actionOnActivate = builder.actionOnActivate

    /**
     * Syntax 1: nodes = [ node1, node2, ..., nodeN ] <br/>
     * or <br/>
     * Syntax 2: nodes = [ [ node1, node2, ..., nodeN ], [ array2 ], ...[ arrayN ] ] <br/>
     */
    private fun parse(nodes: List<Any>): List<List<INode>> {
        // TODO create a real graph of nodes here instead of list.
        val nodes = mutableListOf<List<INode>>()

        // Syntax 1: nodes is an array of SequenceNode (no sub-arrays).
        val nodeList1 = toNodeList(nodes)
        if (nodeList1 != null) {
            nodes.add(nodeList1)
            return nodes
        }

        // Syntax 2: nodes is an array of arrays of SequenceNode (only sub-arrays, only 1 level).
        for (node in nodes) {
            val nodeList2 = toNodeList(node)
            if (nodeList2 != null) {
                nodes.add(nodeList2)
                continue
            }
            // This is not an array of just SequenceNodes, so can't convert to a List.
            throw IllegalArgumentException(
                    "Expected [ [ node1, node2 ], [ node3...] ] but got sub-array containing "
                            + node::class.java.simpleName)
        }
        return nodes

        // TBD: route nodes should have more than 1 node in each branch (warning level).
        // TBD: construct a linked graph, not just a list-of-lists.
        // TBD: fail on isolated islands in graph.
        // TBD: pretty-print the graph for debug output.
    }

    private fun toNodeList(nodes: List<Any>) : List<INode>? {
        //DEBUG println "toNodeList = ${(nodes instanceof Iterable<?>)}, ${nodes.class} -> $nodes"

        val nodeList = mutableListOf<INode>()
        for (node in nodes) {
            if (node is INode) {
                nodeList.add(node)
            } else {
                // This is not an array of just SequenceNodes, so can't convert to a List.
                return null
            }
        }
        return nodeList
    }


}
