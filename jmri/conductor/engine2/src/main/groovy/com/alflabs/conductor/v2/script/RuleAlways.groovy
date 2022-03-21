package com.alflabs.conductor.v2.script

import com.alflabs.annotations.NonNull
/** A synthetic rule which condition is always true. */
class RuleAlways implements IRule {
    private final Closure mAction

    RuleAlways(@NonNull @DelegatesTo(RootScript) Closure action) {
        this.mAction = action
    }

    @Override
    boolean evaluateCondition() {
        return true
    }

    @Override
    void evaluateAction(@NonNull RootScript rootScript) {
        def code = mAction.rehydrate(rootScript /*delegate*/, rootScript /*owner*/, rootScript /*this*/)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code.call()
    }
}
