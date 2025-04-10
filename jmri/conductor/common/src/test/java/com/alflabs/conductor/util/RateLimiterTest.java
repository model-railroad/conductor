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

import com.alflabs.utils.FakeClock;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class RateLimiterTest {

    private FakeClock mClock;

    @Before
    public void setUp() throws Exception {
        mClock = new FakeClock(1000);
    }

    @Test
    public void testRateLimit10Hz() throws Exception {
        // 10 Hz: target loop time is 100 ms
        RateLimiter limiter = new RateLimiter(10.0f, mClock);

        assertThat(mClock.elapsedRealtime()).isEqualTo(1000);

        limiter.limit();
        assertThat(mClock.elapsedRealtime()).isEqualTo(1000);

        // A loop that does nothing... sleeps till target time
        limiter.limit();
        assertThat(mClock.elapsedRealtime()).isEqualTo(1100);

        // A short loop less than target time, limiter should compensate
        mClock.add(20);
        assertThat(mClock.elapsedRealtime()).isEqualTo(1120);
        limiter.limit();
        assertThat(mClock.elapsedRealtime()).isEqualTo(1200);

        // A short loop longer than target time, limiter should skip
        mClock.add(200);
        limiter.limit();
        assertThat(mClock.elapsedRealtime()).isEqualTo(1400);
    }
}
