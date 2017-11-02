package com.alflabs.conductor.script;

import com.alflabs.kv.IKeyValue;
import com.alflabs.manifest.Prefix;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

@AutoFactory(allowSubclasses = true)
public class Var implements IConditional, IIntFunction, IIntValue, IStringValue, IExecEngine, IExportable, IResettable {

    private final String mKeyName;
    private final IKeyValue mKeyValue;
    private final int mInitialIntValue;
    private final String mInitialStringValue;

    private int mIntValue;
    private String mStringValue;
    private boolean mExported;

    public Var(int intValue,
               String scriptName,
               @Provided IKeyValue keyValue) {
        mInitialStringValue = null;
        mInitialIntValue = intValue;
        mIntValue = intValue;
        mKeyName = Prefix.Var + scriptName;
        mKeyValue = keyValue;
    }

    public Var(String stringValue,
               String scriptName,
               @Provided IKeyValue keyValue) {
        mInitialStringValue = stringValue;
        mInitialIntValue = 0;
        mStringValue = stringValue;
        mKeyName = Prefix.Var + scriptName;
        mKeyValue = keyValue;
    }

    @Override
    public void reset() {
        mIntValue = mInitialIntValue;
        mStringValue = mInitialStringValue;
    }

    @Override
    public boolean isActive() {
        return mIntValue != 0;
    }

    public boolean isInt() {
        return mInitialStringValue == null;
    }

    public boolean isString() {
        return mInitialStringValue != null;
    }

    @Override
    public int getAsInt() {
        if (mStringValue != null) {
            try {
                return Integer.parseInt(mStringValue);
            } catch (Exception ignore) {}
        }
        return mIntValue;
    }

    /** Gets the value as a String. */
    @Override
    public String get() {
        if (mStringValue != null) {
            return mStringValue;
        }
        return Integer.toString(mIntValue);
    }

    @Override
    public void accept(int value) {
        mIntValue = value;
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
            mKeyValue.putValue(mKeyName, Integer.toString(mIntValue), true /*broadcast*/);
        }
    }
}
