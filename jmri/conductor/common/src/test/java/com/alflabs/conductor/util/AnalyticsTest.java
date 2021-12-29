package com.alflabs.conductor.util;

import com.alflabs.kv.IKeyValue;
import com.alflabs.utils.FakeFileOps;
import com.alflabs.utils.FileOps;
import com.alflabs.utils.ILogger;
import com.alflabs.utils.MockClock;
import com.google.common.base.Charsets;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okio.Buffer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.google.common.truth.Truth.assertThat;

public class AnalyticsTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock private ILogger mLogger;
    @Mock private IKeyValue mKeyValue;
    @Mock private Random mRandom;
    @Mock private OkHttpClient mOkHttpClient;

    private MockClock mClock = new MockClock();
    private final FileOps mFileOps = new FakeFileOps();
    private final ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();
    private Analytics mAnalytics;

    @Before
    public void setUp() {
        mAnalytics = new Analytics(mLogger, mClock, mFileOps, mKeyValue, mOkHttpClient, mRandom, mExecutor);
    }

    @After
    public void tearDown() throws Exception {
        mAnalytics.shutdown();
    }

    @Test
    public void testSetTrackingId_FromString() throws IOException {
        assertThat(mAnalytics.getAnalyticsId()).isNull();

        mAnalytics.setAnalyticsId("___ UID -string 1234 'ignored- 5 # Comment \nBlah");
        assertThat(mAnalytics.getAnalyticsId()).isEqualTo("UID-1234-5");
    }

    @Test
    public void testSetTrackingId_FromFile() throws IOException {
        assertThat(mAnalytics.getAnalyticsId()).isNull();

        mFileOps.writeBytes(
                "___ UID -string 1234 'ignored- 5 # Comment \n Blah".getBytes(Charsets.UTF_8),
                new File("/tmp/id.txt"));

        mAnalytics.setAnalyticsId("@/tmp/id.txt");
        assertThat(mAnalytics.getAnalyticsId()).isEqualTo("UID-1234-5");
    }

    @Test
    public void testSendEvent() throws Exception {
        Mockito.when(mRandom.nextInt()).thenReturn(42);

        mAnalytics.setAnalyticsId("UID-1234-5");
        mAnalytics.sendEvent("CAT", "ACT", "LAB", "USR");
        mAnalytics.shutdown(); // forces pending tasks to execute

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        Mockito.verify(mOkHttpClient).newCall(requestCaptor.capture());
        Request req = requestCaptor.getValue();
        assertThat(req).isNotNull();
        assertThat(req.url().toString()).isEqualTo("https://www.google-analytics.com/collect");
        assertThat(req.method()).isEqualTo("POST");
        Buffer bodyBuffer = new Buffer();
        //noinspection ConstantConditions
        req.body().writeTo(bodyBuffer);
        assertThat(bodyBuffer.readUtf8()).isEqualTo(
                "v=1&tid=UID-1234-5&ds=consist&cid=2b6cc9c3-0eaa-39c1-8909-1ea928529cbd&t=event&ec=CAT&ea=ACT&el=LAB&z=42&qt=0");
    }
}
