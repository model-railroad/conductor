package com.alflabs.conductor.parser;

import com.alflabs.conductor.script.IIntValue;

class LiteralInt implements IIntValue {
    private final int mValue;

    public LiteralInt(int value) {
        mValue = value;
    }

    @Override
    public int getAsInt() {
        return mValue;
    }
}
