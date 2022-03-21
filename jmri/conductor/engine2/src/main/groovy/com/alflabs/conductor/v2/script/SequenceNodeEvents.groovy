package com.alflabs.conductor.v2.script

import com.alflabs.annotations.NonNull

class SequenceNodeEvents {
    private Optional<IRule> mOnEnterRule = Optional.empty()
    private Optional<IRule> mWhileOccupiedRule = Optional.empty()
    private Optional<IRule> mOnTrailingRule = Optional.empty()
    private Optional<IRule> mOnEmptyRule = Optional.empty()

    void onEnter(@NonNull @DelegatesTo(RootScript) Closure action) {
        mOnEnterRule = Optional.of(new RuleAlways(action))
    }

    @NonNull
    Optional<IRule> getOnEnterRule() {
        return mOnEnterRule
    }

    void whileOccupied(@NonNull @DelegatesTo(RootScript) Closure action) {
        mWhileOccupiedRule = Optional.of(new RuleAlways(action))
    }

    @NonNull
    Optional<IRule> getWhileOccupiedRule() {
        return mWhileOccupiedRule
    }

    void onTrailing(@NonNull @DelegatesTo(RootScript) Closure action) {
        mOnTrailingRule = Optional.of(new RuleAlways(action))
    }

    @NonNull
    Optional<IRule> getOnTrailingRule() {
        return mOnTrailingRule
    }

    void onEmpty(@NonNull @DelegatesTo(RootScript) Closure action) {
        mOnEmptyRule = Optional.of(new RuleAlways(action))
    }

    @NonNull
    Optional<IRule> getOnEmptyRule() {
        return mOnEmptyRule
    }
}
