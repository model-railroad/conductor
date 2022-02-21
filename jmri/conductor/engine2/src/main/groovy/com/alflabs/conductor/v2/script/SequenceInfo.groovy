package com.alflabs.conductor.v2.script

class SequenceInfo {
    Throttle mThrottle
    int mTimeout
    final List<SequenceNode> mNodes = new ArrayList<>()

    void setThrottle(Throttle throttle) {
        mThrottle = throttle
    }

    void setTimeout(int timeout) {
        mTimeout = timeout
    }

    void setNodes(SequenceNode[] nodes) {
        mNodes.addAll(nodes)
    }
}
