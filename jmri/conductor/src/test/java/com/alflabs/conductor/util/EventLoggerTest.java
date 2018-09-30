package com.alflabs.conductor.util;

import com.alflabs.utils.FakeFileOps;
import com.alflabs.utils.FileOps;
import com.alflabs.utils.IClock;
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

    @Mock ILogger mLogger;

    private FileOps mFileOps = new FakeFileOps();
    private IClock mClock;
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

        mEventLogger.shutdown();

        assertThat(mFileOps.isFile(new File(logFile))).isTrue();
        assertThat(mFileOps.toString(new File(logFile), Charsets.UTF_8)).isEqualTo(
                "13:42:43.000 S S1 Before_start_1\n" +
                "13:42:43.000 S S2 Before_start_2\n" +
                "13:42:43.000 V V3 \"Value 3\"\n" +
                "13:42:43.000 D 444 28\n" +
                "13:42:43.000 T T5 On\n");
    }
}
