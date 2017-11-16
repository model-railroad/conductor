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

public class FrequencyMeasurerTest {

    private FakeClock mClock;
    private FrequencyMeasurer mFreq;

    @Before
    public void setUp() throws Exception {
        mClock = new FakeClock(1000);
        mFreq = new FrequencyMeasurer(mClock);
    }

    @Test
    public void testFreq1() throws Exception {
        assertThat(mFreq.getFrequency()).isWithin(0.f).of(0.f);

        mFreq.ping();
        mFreq.ping();
        assertThat(mFreq.getFrequency()).isWithin(0.f).of(0.f);

        mClock.add(1000);
        mFreq.ping();
        assertThat(mFreq.getFrequency()).isWithin(.1f).of(1.f);

        mClock.add(100);
        mFreq.ping();
        assertThat(mFreq.getFrequency()).isWithin(.1f).of(2.5f);

        mClock.add(100);
        mFreq.ping();
        assertThat(mFreq.getFrequency()).isWithin(.1f).of(5.f);

        mClock.add(100);
        mFreq.ping();
        assertThat(mFreq.getFrequency()).isWithin(.1f).of(7.5f);

        mClock.add(100);
        mFreq.ping();
        assertThat(mFreq.getFrequency()).isWithin(.1f).of(9.0f);

        mClock.add(100);
        mFreq.ping();
        assertThat(mFreq.getFrequency()).isWithin(.1f).of(9.7f);

        mClock.add(100);
        mFreq.ping();
        assertThat(mFreq.getFrequency()).isWithin(.1f).of(9.9f);

        mClock.add(100);
        mFreq.ping();
        assertThat(mFreq.getFrequency()).isWithin(.1f).of(10.f);

        mClock.add(100);
        mFreq.ping();
        assertThat(mFreq.getFrequency()).isWithin(.1f).of(10.f);
    }
}
