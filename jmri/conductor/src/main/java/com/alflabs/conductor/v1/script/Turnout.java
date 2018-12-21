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

import com.alflabs.manifest.Constants;
import com.alflabs.manifest.Prefix;
import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.IJmriTurnout;
import com.alflabs.kv.IKeyValue;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

import javax.inject.Inject;

/**
 * A turnout defined by a script.
 * <p/>
 * The actual JMRI turnout is only assigned via the {@link #onExecStart()} method.
 * <p/>
 * JMRI is only used as a setter. We don't use the JMRI turnout state and instead cache
 * the last state set. The default state is normal.
 * <p/>
 * When used as a conditional, a turnout is true in its "normal" state and false
 * in reverse.
 */
@AutoFactory(allowSubclasses = true)
public class Turnout implements IConditional, IExecEngine {

    private final String mJmriName;
    private final String mKeyName;
    private final IJmriProvider mJmriProvider;
    private final IKeyValue mKeyValue;

    private IJmriTurnout mTurnout;
    private boolean mIsNormal = true;

    /**
     * Possible keywords for a turnout function.
     * Must match IIntFunction in the {@link Turnout} implementation.
     */
    public enum Function {
        NORMAL,
        REVERSE
    }

    /** Creates a new turnout for the given JMRI system name. */
    @Inject
    public Turnout(
            String jmriName,
            String scriptName,
            @Provided IJmriProvider jmriProvider,
            @Provided IKeyValue keyValue) {
        mJmriName = jmriName;
        mKeyName = Prefix.Turnout + scriptName;
        mJmriProvider = jmriProvider;
        mKeyValue = keyValue;
    }

    /** Initializes the underlying JMRI turnout. */
    @Override
    public void onExecStart() {
        mTurnout = mJmriProvider.getTurnout(mJmriName);
        onExecHandle();
    }

    public IIntFunction createFunction(Function function) {
        switch (function) {
        case NORMAL:
            return ignored -> setTurnout(IJmriTurnout.NORMAL);
        case REVERSE:
            return ignored -> setTurnout(IJmriTurnout.REVERSE);
        }
        throw new IllegalArgumentException();
    }

    private void setTurnout(boolean normal) {
        mIsNormal = normal;
        if (mTurnout != null) {
            mTurnout.setTurnout(normal);
        }
    }

    @Override
    public boolean isActive() {
        return mIsNormal;
    }

    @Override
    public void onExecHandle() {
        if (mTurnout != null) {
            mIsNormal = mTurnout.isNormal();
        }
        mKeyValue.putValue(mKeyName, mIsNormal ? Constants.Normal : Constants.Reverse, true /*broadcast*/);
    }
}
