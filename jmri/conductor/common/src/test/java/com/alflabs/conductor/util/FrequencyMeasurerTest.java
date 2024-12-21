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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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
        List<Float> actFreqs = new ArrayList<>();
        List<Float> maxFreqs = new ArrayList<>();

        Callable<Void> collect = () -> {
            actFreqs.add(mFreq.getActualFrequency());
            maxFreqs.add(mFreq.getMaxFrequency());
            return null;
        };


        collect.call();
        mFreq.startWork();

        for (int i = 0; i < 10; i++) {
            mClock.add(10);
            mFreq.startWork();
            mClock.add(10);
            mFreq.endWork();
            collect.call();
        }

        assertThat(actFreqs.stream()
                .map(f -> String.format(Locale.US,  "%.1f", f))
                .collect(Collectors.joining(", ")))
                .isEqualTo("0.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0, 55.0");
        assertThat(maxFreqs.stream()
                .map(f -> String.format(Locale.US,  "%.1f", f))
                .collect(Collectors.joining(", ")))
                .isEqualTo("0.0, 50.0, 66.7, 75.0, 80.0, 83.3, 85.7, 87.5, 88.9, 90.0, 100.0");

        actFreqs.clear();
        maxFreqs.clear();
        for (int i = 0; i < 12; i++) {
            mClock.add(100);
            mFreq.startWork();
            mClock.add(100);
            mFreq.endWork();
            collect.call();
        }

        assertThat(actFreqs.stream()
                .map(f -> String.format(Locale.US,  "%.1f", f))
                .collect(Collectors.joining(", ")))
                .isEqualTo("45.9, 41.4, 36.9, 32.4, 27.9, 23.4, 18.9, 14.4, 9.9, 5.4, 5.0, 5.0");
        assertThat(maxFreqs.stream()
                .map(f -> String.format(Locale.US,  "%.1f", f))
                .collect(Collectors.joining(", ")))
                .isEqualTo("91.0, 82.0, 73.0, 64.0, 55.0, 46.0, 37.0, 28.0, 19.0, 10.0, 10.0, 10.0");
    }
}
