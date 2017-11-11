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

import com.alflabs.kv.IKeyValue;
import com.alflabs.manifest.Prefix;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@AutoFactory(allowSubclasses = true, className = "EnumFactory")
public class Enum_ implements IStringFunction, IStringValue, IExecEngine, IExportable, IResettable {

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
    public void reset() {
        mValue = mValues.get(0);
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
