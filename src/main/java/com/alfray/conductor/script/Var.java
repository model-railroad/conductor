package com.alfray.conductor.script;

public class Var implements IConditional {

    private final String mName;
    private int mValue;

    public Var(String name, int value) {
        mName = name;
        mValue = value;
    }

    @Override
    public boolean isActive() {
        return mValue != 0;
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        mValue = value;
    }
}
