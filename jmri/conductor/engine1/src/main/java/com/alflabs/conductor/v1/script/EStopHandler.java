/*
 * Project: Conductor
 * Copyright (C) 2018 alf.labs gmail com,
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


import com.alflabs.conductor.v1.dagger.ScriptScope;
import com.alflabs.kv.IKeyValue;
import com.alflabs.manifest.Constants;

import javax.inject.Inject;

/**
 * Handles the current EStop State shared with RTAC.
 * This is read by the {@link ExecEngine}.
 * It can be set via the {@link Script} "ESTOP" command.
 * <p/>
 * When activated, nothing happens here except setting the proper KV value.
 * The engine will read the new state at the next runtime exec loop and actually send
 * an EStop via the JMRI interface to all defined throtlles. RTAC provides a way to
 * reset the state, which is also handled by the {@link ExecEngine} runtime loop.
 */
@ScriptScope
public class EStopHandler {
    private final IKeyValue mKeyValue;

    private Constants.EStopState mLastEStopState;

    @Inject
    public EStopHandler(IKeyValue keyValue) {
        mKeyValue = keyValue;
    }

    /**
     * Returns true if The EStop-State is defined and Normal.
     * <p/>
     * For a more predictable behavior, the absence of the EStop-State is treated as
     * a active case. This is one of these "should not happen" scenarios.
     */
    public Constants.EStopState getEStopState() {
        final String value = mKeyValue.getValue(Constants.EStopKey);
        if (value == null) return Constants.EStopState.ACTIVE;
        try {
            return Constants.EStopState.valueOf(value);
        } catch (IllegalArgumentException ignore) {}
        return Constants.EStopState.ACTIVE;
    }

    public void activateEStop() {
        final String value = mKeyValue.getValue(Constants.EStopKey);
        if (!Constants.EStopState.ACTIVE.toString().equals(value)) {
            mKeyValue.putValue(Constants.EStopKey, Constants.EStopState.ACTIVE.toString(), true /* broadcast */);
        }
    }

    public void reset() {
        mLastEStopState = Constants.EStopState.NORMAL;
        mKeyValue.putValue(Constants.EStopKey, mLastEStopState.toString(), true /* broadcast */);
    }

    public Constants.EStopState getLastEStopState() {
        return mLastEStopState;
    }

    public void setLastEStopState(Constants.EStopState lastEStopState) {
        mLastEStopState = lastEStopState;
    }
}
