package com.alflabs.conductor.script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class Enum_ implements IStringFunction, IStringValue {

    private final List<String> mValues = new ArrayList<>();
    private String mValue;

    public Enum_(Collection<String> values) {
        mValues.addAll(values);
        mValue = mValues.get(0);
    }

    @Override
    public String get() {
        return mValue;
    }

    public List<String> getValues() {
        return mValues;
    }

    @Override
    public void accept(String value) {
        value = value.toLowerCase(Locale.US);
        if (mValues.contains(value)) {
            mValue = value;
        } else {
            throw new IllegalArgumentException("Invalid value '" + value + "'.");
        }
    }

    public IConditional createCondition(String op, String rhs) {
        if (op.equals("==")) {
            return () -> mValue.equalsIgnoreCase(rhs);
        } else if (op.equals("!=")) {
            return () -> !mValue.equalsIgnoreCase(rhs);
        }
        throw new IllegalArgumentException();
    }

}
