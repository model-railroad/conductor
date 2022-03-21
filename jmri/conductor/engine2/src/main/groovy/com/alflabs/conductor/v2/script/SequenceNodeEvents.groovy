package com.alflabs.conductor.v2.script

class SequenceNodeEvents {
    private IRule mOnEnterRule
    private IRule mWhileOccupiedRule
    private IRule mOnTrailingRule
    private IRule mOnEmptyRule

    void onEnter(@DelegatesTo(RootScript) Closure action) {
        mOnEnterRule = new RuleAlways(action)
    }

    IRule getOnEnterRule() {
        return mOnEnterRule
    }

    void whileOccupied(@DelegatesTo(RootScript) Closure action) {
        mWhileOccupiedRule = new RuleAlways(action)
    }

    IRule getWhileOccupiedRule() {
        return mWhileOccupiedRule
    }

    void onTrailing(@DelegatesTo(RootScript) Closure action) {
        mOnTrailingRule = new RuleAlways(action)
    }

    IRule getOnTrailingRule() {
        return mOnTrailingRule
    }

    void onEmpty(@DelegatesTo(RootScript) Closure action) {
        mOnEmptyRule = new RuleAlways(action)
    }

    IRule getOnEmptyRule() {
        return mOnEmptyRule
    }
}
