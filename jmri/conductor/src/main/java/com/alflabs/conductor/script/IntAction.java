package com.alflabs.conductor.script;

/**
 * Represents one action, which is composed of a function (setter) and value (getter).
 */
class IntAction implements IAction {
    private final IIntFunction mFunction;
    private final IIntValue mValue;

    IntAction(IIntFunction function, IIntValue value) {
        mFunction = function;
        mValue = value;
    }

    @Override
    public void execute() {
        mFunction.accept(mValue.getAsInt());
    }
}
