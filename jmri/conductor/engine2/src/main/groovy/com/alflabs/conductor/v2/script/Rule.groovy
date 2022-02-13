package com.alflabs.conductor.v2.script

class Rule {
    private final Closure<Boolean> mCondition
    private Closure mAction

    Rule(Closure<Boolean> condition) {
        this.mCondition = condition
    }

    void then(@DelegatesTo(RootScript) Closure action) {
        this.mAction = action
    }

    boolean evaluateCondition() {
        return mCondition.call()
    }

    void evaluateAction() {
        mAction.call()
    }
}
