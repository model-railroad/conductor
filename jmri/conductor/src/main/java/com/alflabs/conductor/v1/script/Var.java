/*
 * Project: Conductor
 * Copyright (C) 2017 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alflabs.conductor.v1.script;

import com.alflabs.annotations.NonNull;
import com.alflabs.kv.IKeyValue;
import com.alflabs.manifest.Prefix;
import com.alflabs.rx.ISubscriber;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

@AutoFactory(allowSubclasses = true)
public class Var implements IConditional, IIntValue, IStringValue, IExecEngine, IExportable, IImportable, IResettable {

    private final String mKeyName;
    private final IKeyValue mKeyValue;
    private final int mInitialIntValue;
    private final String mInitialStringValue;
    private final IIntValue mIntValueSupplier;

    private int mIntValue;
    private String mStringValue;
    private boolean mExported;
    private ISubscriber<String> mImportSubscriber;

    public Var(int intValue,
               String varName,
               @Provided IKeyValue keyValue) {
        mInitialStringValue = null;
        mInitialIntValue = intValue;
        mIntValueSupplier = null;
        mIntValue = intValue;
        mKeyName = Prefix.Var + varName;
        mKeyValue = keyValue;
    }

    public Var(IIntValue intValueSupplier,
               String varName,
               @Provided IKeyValue keyValue) {
        mIntValueSupplier = intValueSupplier;
        mInitialStringValue = null;
        int intValue = intValueSupplier.getAsInt();
        mInitialIntValue = intValue;
        mIntValue = intValue;
        mKeyName = Prefix.Var + varName;
        mKeyValue = keyValue;
    }

    public Var(String stringValue,
               String scriptName,
               @Provided IKeyValue keyValue) {
        mInitialStringValue = stringValue;
        mInitialIntValue = 0;
        mIntValueSupplier = null;
        mStringValue = stringValue;
        mKeyName = Prefix.Var + scriptName;
        mKeyValue = keyValue;
    }

    @Override
    public void reset() {
        mIntValue = mIntValueSupplier == null ? mInitialIntValue : mIntValueSupplier.getAsInt();
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
    public boolean isExported() {
        return mExported;
    }

    @Override
    public boolean isImported() {
        return mImportSubscriber != null;
    }

    @Override
    public int getAsInt() {
        if (mStringValue != null) {
            try {
                return Integer.parseInt(mStringValue);
            } catch (Exception ignore) {}
        }
        return mIntValueSupplier == null ? mIntValue : mIntValueSupplier.getAsInt();
    }

    /** Gets the value as a String. */
    @Override
    public String get() {
        if (mStringValue != null) {
            return mStringValue;
        } else {
            return Integer.toString(getAsInt());
        }
    }

    @NonNull
    public IConditional createCondition(String op, Var rhs) {
        if (op.equals("==")) {
            return () -> get().equalsIgnoreCase(rhs.get());
        } else if (op.equals("!=")) {
            return () -> !get().equalsIgnoreCase(rhs.get());
        }
        throw new IllegalArgumentException();
    }

    @NonNull
    public IStringFunction createSetStrFunction() {
        return value -> mStringValue = value;
    }

    @NonNull
    public IIntFunction createSetIntFunction() {
        // Ignored when mIntValueSupplier != null
        return value -> mIntValue = value;
    }

    @NonNull
    public IIntFunction createIncFunction() {
        // Ignored when mIntValueSupplier != null
        return value -> mIntValue += value;
    }

    @NonNull
    public IIntFunction createDecFunction() {
        // Ignored when mIntValueSupplier != null
        return value -> mIntValue -= value;
    }

    @Override
    public void setExported(boolean exported) {
        mExported = exported;
    }

    @Override
    public void setImported(boolean imported) {
        if (imported && mImportSubscriber == null) {
            mImportSubscriber = (stream, key) -> {
                if (mKeyName.equals(key)) {
                    String value = mKeyValue.getValue(mKeyName);
                    if (isString()) {
                        mStringValue = value;
                    } else {
                        try {
                            // Ignored when mIntValueSupplier != null
                            mIntValue = Integer.parseInt(value);
                        } catch (Exception ignore) {}
                    }
                }
            };
            mKeyValue.getChangedStream().subscribe(mImportSubscriber);

        } else if (!imported && mImportSubscriber != null) {
            mKeyValue.getChangedStream().remove(mImportSubscriber);
            mImportSubscriber = null;
        }
    }

    @Override
    public void onExecStart() {
        onExecHandle();
    }

    @Override
    public void onExecHandle() {
        if (mExported) {
            mKeyValue.putValue(mKeyName, get(), true /*broadcast*/);
        }
    }
}
