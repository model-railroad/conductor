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
    private long mLastPingMs;
    private long mDelayMs;

    public FrequencyMeasurer(IClock clock) {
        mClock = clock;
    }

    public void ping() {
        long now = mClock.elapsedRealtime();
        if (mLastPingMs != 0) {
            long delay = now - mLastPingMs;
            // simple averaging: 1/3rd last delay, 2/3rd new delay
            mDelayMs = mDelayMs <= 0 ? delay : (mDelayMs + 2 * delay) / 3;
        }
        mLastPingMs = now;
    }

    public float getFrequency() {
        return mDelayMs <= 0 ? 0 : (1000.0f / mDelayMs);
    }
}
