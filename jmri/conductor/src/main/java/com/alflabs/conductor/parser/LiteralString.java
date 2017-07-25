package com.alflabs.conductor.parser;

import com.alflabs.conductor.script.IStringValue;

class LiteralString implements IStringValue {
    private final String mValue;

    public LiteralString(String value) {
        mValue = value;
    }

    @Override
    public String get() {
        return mValue;
    }
}
