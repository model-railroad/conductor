package com.alflabs.conductor.v2.script

/** A synthetic rule which condition is always true. */
class RuleAlways implements IRule {
    private final Closure mAction

    RuleAlways(@DelegatesTo(RootScript) Closure action) {
        this.mAction = action
    }

    @Override
    boolean evaluateCondition() {
        return true
    }

    @Override
    void evaluateAction() {
        mAction.call()
    }
}
