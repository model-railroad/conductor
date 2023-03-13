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

import com.alflabs.conductor.jmri.IJmriProvider;
import com.alflabs.conductor.jmri.IJmriSensor;
import com.alflabs.conductor.util.EventLogger;
import com.alflabs.kv.IKeyValue;
import com.alflabs.manifest.Constants;
import com.alflabs.manifest.Prefix;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

/**
 * A sensor defined by a script.
 * <p/>
 * The actual JMRI sensor is only assigned via the {@link #onExecStart()} method.
 */
public class Sensor implements IConditional, IExecEngine {

    private final String mJmriName;
    private final String mKeyName;
    private final IJmriProvider mJmriProvider;
    private final IKeyValue mKeyValue;
    private final EventLogger mEventLogger;

    private Runnable mOnChangedListener;
    private IJmriSensor mSensor;
    private boolean mLastActive;

    /** Creates a new sensor for the given JMRI system name. */
    @AssistedInject
    public Sensor(
            @Assisted("jmriName") String jmriName,
            @Assisted("scriptName") String scriptName,
            IJmriProvider jmriProvider,
            IKeyValue keyValue,
            EventLogger eventLogger) {
        mJmriName = jmriName;
        mKeyName = Prefix.Sensor + scriptName;
        mJmriProvider = jmriProvider;
        mKeyValue = keyValue;
        mEventLogger = eventLogger;
    }

    public void setOnChangedListener(Runnable onChangedListener) {
        mOnChangedListener = onChangedListener;
    }

    /** Initializes the underlying JMRI sensor. */
    @Override
    public void onExecStart() {
        mSensor = mJmriProvider.getSensor(mJmriName);
        onExecHandle();
    }

    @Override
    public boolean isActive() {
        return mSensor != null && mSensor.isActive();
    }

    private void setActive(boolean active) {
        if (mSensor != null) {
            mSensor.setActive(active);
        }
    }

    @Override
    public void onExecHandle() {
        boolean active = isActive();
        String value = active ? Constants.On : Constants.Off;
        mKeyValue.putValue(mKeyName, value, true /*broadcast*/);
        if (active != mLastActive) {
            mLastActive = active;
            mEventLogger.logAsync(EventLogger.Type.Sensor, mKeyName, value);
            if (mOnChangedListener != null) {
                mOnChangedListener.run();
            }
        }
    }

    public IAction createAction(boolean isActive) {
        return () -> setActive(isActive);
    }

    public IJmriSensor getJmriSensor() {
        return mSensor;
    }
}
