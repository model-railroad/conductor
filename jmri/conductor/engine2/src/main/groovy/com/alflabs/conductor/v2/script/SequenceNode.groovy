package com.alflabs.conductor.v2.script

class SequenceNode {
    private final Block mBlock
    private final SequenceNodeEvents mEvents

    SequenceNode(Block block, @DelegatesTo(SequenceNodeEvents) Closure cl) {
        this.mBlock = block
        this.mEvents = new SequenceNodeEvents()
        def code = cl.rehydrate(mEvents /*delegate*/, this /*owner*/, this /*this*/)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code.call()
    }

    Block getBlock() {
        return mBlock
    }

    SequenceNodeEvents getEvents() {
        return mEvents
    }
}
