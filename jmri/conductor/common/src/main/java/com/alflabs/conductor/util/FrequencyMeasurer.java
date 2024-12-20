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

package com.alflabs.conductor.util;

import com.alflabs.utils.IClock;

public class FrequencyMeasurer {

    private final IClock mClock;
    private long mLastPingNanos;
    private long mDelayNanos;
    private long mWorkNanos;
    private float mFreqActual;
    private float mFreqMax;

    public FrequencyMeasurer(IClock clock) {
        mClock = clock;
    }

    public void startWork() {
        long nowNanos = mClock.nanoTime();
        if (mLastPingNanos != 0) {
            mDelayNanos = nowNanos - mLastPingNanos;
        }
        mLastPingNanos = nowNanos;
    }

    public void endWork() {
        if (mLastPingNanos != 0) {
            long nowNanos = mClock.nanoTime();
            mWorkNanos = nowNanos - mLastPingNanos;
        }
    }

    public float getActualFrequency() {
        float delayFreq = mDelayNanos <= 0 ? 0 : (float) ((double)1e9 / mDelayNanos);
        // simple averaging: 1/3rd last value, 2/3rd new value
        mFreqActual = mFreqActual <= 0 ? delayFreq : (mFreqActual + 2 * delayFreq) / 3;
        return mFreqActual;
    }

    public float getMaxFrequency() {
        float workFreq = mWorkNanos <= 0 ? 0 : (float) ((double)1e9 / mWorkNanos);
        // simple averaging: 1/3rd last value, 2/3rd new value
        mFreqMax = mFreqMax <= 0 ? workFreq : (mFreqMax + 2 * workFreq) / 3;
        return mFreqMax;
    }
}
