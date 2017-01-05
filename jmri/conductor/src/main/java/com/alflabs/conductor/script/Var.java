package com.alflabs.conductor.script;

public class Var implements IConditional, IIntFunction, IIntValue {

    private int mValue;

    public Var(int value) {
        mValue = value;
    }

    @Override
    public boolean isActive() {
        return mValue != 0;
    }

    @Override
    public int getAsInt() {
        return mValue;
    }

    @Override
    public void accept(int value) {
        mValue = value;
    }
}
