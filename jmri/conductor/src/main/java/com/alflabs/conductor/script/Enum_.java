package com.alflabs.conductor.script;

import com.alflabs.kv.IKeyValue;
import com.alflabs.manifest.Prefix;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@AutoFactory(allowSubclasses = true, className = "EnumFactory")
public class Enum_ implements IStringFunction, IStringValue, IExecEngine, IExportable {

    private final String mKeyName;
    private final IKeyValue mKeyValue;

    private final List<String> mValues = new ArrayList<>();
    private String mValue;
    private boolean mExported;

    public Enum_(Collection<String> values,
                 String scriptName,
                 @Provided IKeyValue keyValue) {
        mValues.addAll(values);
        mValue = mValues.get(0);
        mKeyName = Prefix.Var + scriptName;
        mKeyValue = keyValue;
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

    @Override
    public void setExported(boolean exported) {
        mExported = exported;
    }

    @Override
    public void onExecStart() {
        onExecHandle();
    }

    @Override
    public void onExecHandle() {
        if (mExported) {
            mKeyValue.putValue(mKeyName, mValue, true /*broadcast*/);
        }
    }
}
