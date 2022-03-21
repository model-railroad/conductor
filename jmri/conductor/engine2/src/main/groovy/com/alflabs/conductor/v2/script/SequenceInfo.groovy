package com.alflabs.conductor.v2.script

import com.alflabs.annotations.NonNull

class SequenceInfo {
    private Optional<Throttle> mThrottle = Optional.empty()
    private int mTimeout
    private final List<List<SequenceNode>> mNodes = new ArrayList<>()
    private Optional<IRule> mOnActivateRule = Optional.empty()

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
    Optional<IRule> getOnActivateRule() {
        return mOnActivateRule
    }
}
