package com.alflabs.conductor.script;

/**
 * Represents one event condition, which is composed of a conditional and can be negated.
 */
class Cond {
    private final IConditional mConditional;
    private final boolean mNegated;

    Cond(IConditional conditional, boolean negated) {
        mConditional = conditional;
        mNegated = negated;
    }

    boolean eval() {
        boolean status = mConditional.isActive();
        if (mNegated) {
            status = !status;
        }
        return status;
    }
}
