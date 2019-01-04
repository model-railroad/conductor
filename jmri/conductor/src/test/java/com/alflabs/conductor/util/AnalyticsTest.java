package com.alflabs.conductor.util;

import com.alflabs.kv.IKeyValue;
import com.alflabs.utils.FakeFileOps;
import com.alflabs.utils.FileOps;
import com.alflabs.utils.ILogger;
import com.google.common.base.Charsets;
import okhttp3.OkHttpClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;

public class AnalyticsTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock private ILogger mLogger;
    @Mock private IKeyValue mKeyValue;
    @Mock private OkHttpClient mOkHttpClient;

    private FileOps mFileOps = new FakeFileOps();
    private Analytics mAnalytics;

    @Before
    public void setUp() throws Exception {
        mAnalytics = new Analytics(mLogger, mFileOps, mKeyValue, mOkHttpClient);
    }

    @Test
    public void testSetTrackingId_FromString() throws IOException {
        assertThat(mAnalytics.getTrackingId()).isNull();

        mAnalytics.setTrackingId("___ UID -string 1234 'ignored- 5 # Comment \nBlah");
        assertThat(mAnalytics.getTrackingId()).isEqualTo("UID-1234-5");
    }

    @Test
    public void testSetTrackingId_FromFile() throws IOException {
        assertThat(mAnalytics.getTrackingId()).isNull();

        mFileOps.writeBytes(
                "___ UID -string 1234 'ignored- 5 # Comment \n Blah".getBytes(Charsets.UTF_8),
                new File("/tmp/id.txt"));

        mAnalytics.setTrackingId("@/tmp/id.txt");
        assertThat(mAnalytics.getTrackingId()).isEqualTo("UID-1234-5");
    }
}
