package com.alflabs.conductor.v2.script

class SequenceNode {
    private final Block mBlock
    private final Closure mAction

    SequenceNode(Block block, Closure action) {
        this.mBlock = block
        this.mAction = action
    }
}
