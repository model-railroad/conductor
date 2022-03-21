package com.alflabs.conductor.v2.script

import com.alflabs.annotations.NonNull

class Rule implements IRule {
    private final Closure<Boolean> mCondition
    private Closure mAction

    Rule(@NonNull Closure<Boolean> condition) {
        this.mCondition = condition
    }

    void then(@NonNull @DelegatesTo(RootScript) Closure action) {
        this.mAction = action
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
