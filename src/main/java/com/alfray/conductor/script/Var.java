package com.alfray.conductor.script;

public class Var implements IConditional, IFunction.Int, IValue.Int {

    private int mValue;

    public Var(int value) {
        mValue = value;
    }

    @Override
    public boolean isActive() {
        return mValue != 0;
    }

    @Override
    public Integer getValue() {
        return mValue;
    }

    @Override
    public void setValue(Integer value) {
        mValue = value;
    }
}
