package com.alflabs.conductor.script;

import com.alflabs.kv.IKeyValue;
import com.alflabs.manifest.Prefix;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

@AutoFactory(allowSubclasses = true)
public class VarInt implements IConditional, IIntFunction, IIntValue, IExecEngine, IExportable, IResettable {

    private final String mKeyName;
    private final IKeyValue mKeyValue;
    private final int mInitialValue;

    private int mValue;
    private boolean mExported;

    public VarInt(int value,
                  String scriptName,
                  @Provided IKeyValue keyValue) {
        mInitialValue = value;
        mValue = value;
        mKeyName = Prefix.Var + scriptName;
        mKeyValue = keyValue;
    }

    @Override
    public void reset() {
        mValue = mInitialValue;
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
            mKeyValue.putValue(mKeyName, Integer.toString(mValue), true /*broadcast*/);
        }
    }
}
