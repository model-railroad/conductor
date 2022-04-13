package com.alflabs.conductor.v2.script.impl

import com.alflabs.annotations.NonNull
import com.alflabs.conductor.v2.script.RootScript

/** A condition...then rule. */
class Rule implements IEvalRule {
    private final Closure<Boolean> mCondition
    private Closure mAction

    Rule(@NonNull Closure<Boolean> condition) {
        mCondition = condition
    }

    void then(@NonNull @DelegatesTo(RootScript) Closure action) {
        mAction = action
    }

    @Override
    boolean evaluateCondition() {
        return mCondition.call()
    }

    @Override
    void evaluateAction(@NonNull RootScript rootScript) {
        def code = mAction.rehydrate(rootScript /*delegate*/, this /*owner*/, this /*this*/)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code.call()
    }
}
