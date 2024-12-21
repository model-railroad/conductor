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
    private final static int AVG_WINDOW = 10;

    private final IClock mClock;
    private long mLastPingNanos;
    private long mDelayNanos;
    private long mWorkNanos;
    private final AvgWindow mFreqActual = new AvgWindow(AVG_WINDOW);
    private final AvgWindow mFreqMax = new AvgWindow(AVG_WINDOW);


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
        mFreqActual.fill(delayFreq);
        return mFreqActual.average();
    }

    public float getMaxFrequency() {
        float workFreq = mWorkNanos <= 0 ? 0 : (float) ((double)1e9 / mWorkNanos);
        mFreqMax.fill(workFreq);
        return mFreqMax.average();
    }

    private static class AvgWindow {
        private final float[] mValues;
        private int mFill;
        private int mDiv;

        public AvgWindow(int size) {
            mValues = new float[size];
        }

        public void fill(float newVal) {
            mValues[mFill++] = newVal;
            int size = mValues.length;
            if (mDiv < size) {
                mDiv++;
            }
            if (mFill == size) {
                mFill = 0;
            }
        }

        public float average() {
            if (mDiv <= 0) {
                return 0;
            }
            float v = 0;
            for (int i = 0; i < mDiv; i++) {
                v += mValues[i];
            }
            return v / mDiv;
        }
    }
}
