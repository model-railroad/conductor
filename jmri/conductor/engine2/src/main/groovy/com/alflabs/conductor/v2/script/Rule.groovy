package com.alflabs.conductor.v2.script

class Rule implements IRule {
    private final Closure<Boolean> mCondition
    private Closure mAction

    Rule(Closure<Boolean> condition) {
        this.mCondition = condition
    }

    void then(@DelegatesTo(RootScript) Closure action) {
        this.mAction = action
    }

    @Override
    boolean evaluateCondition() {
        return mCondition.call()
    }

    @Override
    void evaluateAction(RootScript rootScript) {
        def code = mAction.rehydrate(rootScript /*delegate*/, this /*owner*/, this /*this*/)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code.call()
    }
}
