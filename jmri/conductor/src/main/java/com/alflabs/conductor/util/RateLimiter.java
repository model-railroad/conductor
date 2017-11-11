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

public class RateLimiter {

    private final Now mNow;
    private final long mTargetTimeMs;
    private long mLastTimeMs;

    public RateLimiter(float frequencyHz, Now now) {
        mNow = now;
        mTargetTimeMs = (long) (1000.0f / frequencyHz);
    }

    public void limit() {
        // Iteration N:  ------------->|  pause  |-> (end time) ------>
        // Iteration N+1 ---actual------>| pause |-> (end time) ------>
        if (mLastTimeMs > 0) {
            // Actual time (ms) elapsed since the last mLastTimeMs checkpoint.
            long actual = mNow.now() - mLastTimeMs;
            // Assuming "actual" time was less than target, how long to sleep?
            long pause = mTargetTimeMs - actual;
            mNow.sleep(pause);
        }
        // Checkpoint. This should occur every mTargetTimeMs.
        mLastTimeMs = mNow.now();
    }
}
