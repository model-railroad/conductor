/*
 * Project: Conductor
 * Copyright (C) 2022 alf.labs gmail com,
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

import com.alflabs.utils.FakeFileOps;
import com.alflabs.utils.FileOps;
import com.alflabs.utils.ILogger;
import com.google.common.base.Charsets;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.time.LocalDateTime;

import static com.google.common.truth.Truth.assertThat;

public class EventLoggerTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock private ILogger mLogger;

    private FileOps mFileOps = new FakeFileOps();
    private ILocalDateTimeNowProvider mLocalDateTimeNow;
    private EventLogger mEventLogger;

    @Before
    public void setUp() throws Exception {

        // The Epoch + 1:42:43pm
        LocalDateTime now = LocalDateTime.of(1901, 1, 1, 13, 42, 43);
        mLocalDateTimeNow = () -> now;

        mEventLogger = new EventLogger(mLogger, mFileOps, mLocalDateTimeNow);
    }

    @Test
    public void testLog() throws Exception {
        // Test normal flow: log, start, shutdown.

        mEventLogger.logAsync(EventLogger.Type.Sensor, "S1", "Before_start_1");
        mEventLogger.logAsync(EventLogger.Type.Sensor, "S2", "Before_start_2");

        String logFile = mEventLogger.start(new File("/tmp/testdir"));
        assertThat(logFile).isEqualTo(
                "/tmp/testdir/conductor-log-1901-01-01-13-42-43.txt".replace('/', File.separatorChar));

        mEventLogger.logAsync(EventLogger.Type.Variable, "V3", "Value 3"); // this gets quoted
        mEventLogger.logAsync(EventLogger.Type.DccThrottle, "444", "28");
        mEventLogger.logAsync(EventLogger.Type.Turnout, "T5", "On");
        mEventLogger.logAsync(EventLogger.Type.Timer, "timer-2", "Start:300");

        mEventLogger.shutdown();

        assertThat(mFileOps.isFile(new File(logFile))).isTrue();
        assertThat(mFileOps.toString(new File(logFile), Charsets.UTF_8)).isEqualTo(
                "13:42:43.000 S S1 Before_start_1\n" +
                "13:42:43.000 S S2 Before_start_2\n" +
                "13:42:43.000 V V3 \"Value 3\"\n" +
                "13:42:43.000 D 444 28\n" +
                "13:42:43.000 T T5 On\n" +
                "13:42:43.000 T timer-2 Start:300\n");
    }
}
