package com.alflabs.conductor.v2.script

import com.alflabs.annotations.NonNull

class SequenceNode {
    private final Block mBlock
    private final SequenceNodeEvents mEvents

    SequenceNode(
            @NonNull Block block,
            @NonNull @DelegatesTo(SequenceNodeEvents) Closure cl) {
        this.mBlock = block
        this.mEvents = new SequenceNodeEvents()
        def code = cl.rehydrate(mEvents /*delegate*/, this /*owner*/, this /*this*/)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code.call()
    }

    @NonNull
    Block getBlock() {
        return mBlock
    }

    @NonNull
    SequenceNodeEvents getEvents() {
        return mEvents
    }
}
