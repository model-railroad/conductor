package com.alflabs.conductor.script;

/**
 * Represents one action, which is composed of a function (setter) and value (getter).
 */
class StringAction implements IAction {
    private final IStringFunction mFunction;
    private final IStringValue mValue;

    StringAction(IStringFunction function, IStringValue value) {
        mFunction = function;
        mValue = value;
    }

    @Override
    public void execute() {
        mFunction.accept(mValue.get());
    }
}
