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

import com.alflabs.conductor.util.EventLogger;
import com.alflabs.kv.IKeyValue;
import com.alflabs.manifest.Prefix;
import com.alflabs.rx.ISubscriber;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@AutoFactory(allowSubclasses = true, className = "EnumFactory")
public class Enum_ implements IStringFunction, IStringValue, IExecEngine, IExportable, IImportable, IResettable {

    private final String mKeyName;
    private final IKeyValue mKeyValue;

    private final List<String> mValues = new ArrayList<>();
    private final EventLogger mEventLogger;

    private String mValue;
    private boolean mExported;
    private ISubscriber<String> mImportSubscriber;

    public Enum_(Collection<String> values,
                 String enumName,
                 @Provided IKeyValue keyValue,
                 @Provided EventLogger eventLogger) {
        mEventLogger = eventLogger;
        mValues.addAll(values.stream().map(String::toLowerCase).collect(Collectors.toList()));
        mValue = mValues.get(0);
        mKeyName = Prefix.Var + enumName;
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
            mEventLogger.logAsync(EventLogger.Type.Variable, mKeyName, value);
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
    public boolean isExported() {
        return mExported;
    }

    @Override
    public boolean isImported() {
        return mImportSubscriber != null;
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
                    accept(mKeyValue.getValue(mKeyName));
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
            mKeyValue.putValue(mKeyName, mValue, true /*broadcast*/);
        }
    }
}
