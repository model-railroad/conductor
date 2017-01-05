package com.alflabs.conductor.script;

/**
 * Represents one action, which is composed of a function (setter) and value (getter).
 */
class Action {
    private final IIntFunction mFunction;
    private final IIntValue mValue;

    Action(IIntFunction function, IIntValue value) {
        mFunction = function;
        mValue = value;
    }

    void execute() {
        mFunction.accept(mValue.getAsInt());
    }
}
