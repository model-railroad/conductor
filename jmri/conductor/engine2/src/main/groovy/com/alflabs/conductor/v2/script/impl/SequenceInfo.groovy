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

package com.alflabs.conductor.v2.script.impl

import com.alflabs.annotations.NonNull
import com.alflabs.conductor.v2.script.RootScript

class SequenceInfo {
    private Optional<Throttle> mThrottle = Optional.empty()
    private int mTimeout
    private final List<List<SequenceNode>> mNodes = new ArrayList<>()
    private Optional<IEvalRule> mOnActivateRule = Optional.empty()

    void setThrottle(@NonNull Throttle throttle) {
        mThrottle = Optional.of(throttle)
    }

    @NonNull
    Optional<Throttle> getThrottle() {
        return mThrottle
    }

    void setTimeout(int timeout) {
        mTimeout = timeout
    }

    int getTimeout() {
        return mTimeout
    }

    /**
     * Syntax 1: nodes = [ node1, node2, ..., nodeN ] <br/>
     * or <br/>
     * Syntax 2: nodes = [ [ node1, node2, ..., nodeN ], [ array2 ], ...[ arrayN ] ] <br/>
     */
    void setNodes(@NonNull Object nodes) {
        // Syntax 1: nodes is an array of SequenceNode (no sub-arrays).
        def nodeList = toNodeList(nodes)
        if (nodeList != null) {
            mNodes.add(nodeList)
            return
        }

        // Syntax 2: nodes is an array of arrays of SequenceNode (only sub-arrays, only 1 level).
        for (Object node in nodes) {
            nodeList = toNodeList(node)
            if (nodeList != null) {
                mNodes.add(nodeList)
                continue
            }
            // This is not an array of just SequenceNodes, so can't convert to a List.
            throw new IllegalArgumentException(
                    "Expected [ [ node1, node2 ], [ node3...] ] but got sub-array containing "
                    + node.class.simpleName)
        }

        // TBD: route nodes should have more than 1 node in each branch (warning level).
        // TBD: construct a linked graph, not just a list-of-lists.
        // TBD: fail on isolated islands in graph.
        // TBD: pretty-print the graph for debug output.
    }

    private static List<SequenceNode> toNodeList(Object nodes) {
        //DEBUG println "toNodeList = ${(nodes instanceof Iterable<?>)}, ${nodes.class} -> $nodes"
        if (!(nodes instanceof Iterable<?>)) { return null }
        def nodeList = new ArrayList<SequenceNode>()
        for (Object node in nodes) {
            if (node instanceof SequenceNode) {
                nodeList.add(node)
            } else {
                // This is not an array of just SequenceNodes, so can't convert to a List.
                return null;
            }
        }
        return nodeList
    }

    @NonNull
    List<List<SequenceNode>> getNodes() {
        return mNodes.asUnmodifiable()
    }

    void onActivate(@DelegatesTo(RootScript) Closure action) {
        mOnActivateRule = Optional.of(new RuleAlways(action))
    }

    @NonNull
    Optional<IEvalRule> getOnActivateRule() {
        return mOnActivateRule
    }
}
