package com.alflabs.conductor.v2.script

import com.alflabs.annotations.NonNull

/** A after...then...then_after rule. */
class RuleAfter implements IRule {
    private final Timer mAfterTimer
    private final AndAfterContinuation mAndAfterContinuation
    private Closure mAction

    interface AndAfterContinuation {
        abstract RuleAfter and_after(Timer newTimer)
    }

    RuleAfter(@NonNull Timer afterTimer, @NonNull AndAfterContinuation andAfterContinuation) {
        this.mAfterTimer = afterTimer
        this.mAndAfterContinuation = andAfterContinuation
    }

    AndAfterContinuation then(@NonNull @DelegatesTo(RootScript) Closure action) {
        println "RuleAfter then this = $this"
        mAction = action
        return mAndAfterContinuation
    }

    @Override
    boolean evaluateCondition() {
        return mAfterTimer.isActive()
    }

    @Override
    void evaluateAction(@NonNull RootScript rootScript) {
        println "RuleAfter evaluateAction this = $this"

        def code = mAction.rehydrate(rootScript /*delegate*/, this /*owner*/, this /*this*/)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code.call()
    }
}
