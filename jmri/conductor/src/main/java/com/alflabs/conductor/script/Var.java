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

package com.alflabs.conductor.script;

import com.alflabs.annotations.NonNull;
import com.alflabs.kv.IKeyValue;
import com.alflabs.manifest.Prefix;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

@AutoFactory(allowSubclasses = true)
public class Var implements IConditional, IIntValue, IStringValue, IExecEngine, IExportable, IResettable {

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

    public boolean isExported() {
        return mExported;
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

    @NonNull
    public IIntFunction createSetterFunction() {
        return value -> mIntValue = value;
    }

    @NonNull
    public IIntFunction createIncFunction() {
        return value -> mIntValue += value;
    }

    @NonNull
    public IIntFunction createDecFunction() {
        return value -> mIntValue -= value;
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
