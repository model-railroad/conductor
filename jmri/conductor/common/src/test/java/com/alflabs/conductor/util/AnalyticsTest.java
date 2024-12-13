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
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

public class AnalyticsTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock private ILogger mLogger;
    @Mock private IKeyValue mKeyValue;
    @Mock private Random mRandom;
    @Mock private OkHttpClient mOkHttpClient;
    @Mock private ILocalDateTimeNowProvider mLocalDateTimeNowProvider;

    private MockClock mClock = new MockClock();
    private final FileOps mFileOps = new FakeFileOps();
    private final ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();
    private Analytics mAnalytics;

    @Before
    public void setUp() {
        // Otherwise by default it is permanently 1:42 PM here
        when(mLocalDateTimeNowProvider.getNow()).thenReturn(
                LocalDateTime.of(1901, 2, 3, 13, 42, 43));

        mAnalytics = new Analytics(
                mLogger,
                mClock,
                mRandom,
                mFileOps,
                mKeyValue,
                mOkHttpClient,
                mLocalDateTimeNowProvider,
                mExecutor);
    }

    @After
    public void tearDown() throws Exception {
        mAnalytics.shutdown();
    }

    @Test
    public void ua_SetTrackingId_FromString() throws IOException {
        assertThat(mAnalytics.getAnalyticsId()).isNull();

        mAnalytics.setAnalyticsId("___ UID -string 1234 'ignored- 5 # Comment \nBlah");
        assertThat(mAnalytics.getAnalyticsId()).isEqualTo("UID-1234-5");
    }

    @Test
    public void ua_SetTrackingId_FromFile() throws IOException {
        assertThat(mAnalytics.getAnalyticsId()).isNull();

        mFileOps.writeBytes(
                "___ UID -string 1234 'ignored- 5 # Comment \n Blah".getBytes(Charsets.UTF_8),
                new File("/tmp/id.txt"));

        mAnalytics.setAnalyticsId("@/tmp/id.txt");
        assertThat(mAnalytics.getAnalyticsId()).isEqualTo("UID-1234-5");
    }

    @Test
    public void ua_SendEvent() throws Exception {
        when(mRandom.nextInt()).thenReturn(42);

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

    @Test
    public void ga4_SetTrackingId_FromString() throws IOException {
        assertThat(mAnalytics.getAnalyticsId()).isNull();

        mAnalytics.setAnalyticsId(" G-1234ABCD | 987654321 | XyzAppSecretZyX # Comment \nBlah");
        assertThat(mAnalytics.getAnalyticsId()).isEqualTo("G-1234ABCD");
    }

    @Test
    public void ga4_SetTrackingId_FromFile() throws IOException {
        assertThat(mAnalytics.getAnalyticsId()).isNull();

        mFileOps.writeBytes(
                " G-1234|56789|Secret # Comment \nBlah".getBytes(Charsets.UTF_8),
                new File("/tmp/id.txt"));

        mAnalytics.setAnalyticsId("@/tmp/id.txt");
        assertThat(mAnalytics.getAnalyticsId()).isEqualTo("G-1234");
    }

    @Test
    public void ga4_SendEvent() throws Exception {
        when(mRandom.nextInt()).thenReturn(42);

        mAnalytics.setAnalyticsId(" G-1234ABCD | 987654321 | XyzAppSecretZyX ");
        mAnalytics.sendEvent("CAT", "ACT", "LAB", "72");
        mAnalytics.shutdown(); // forces pending tasks to execute

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        Mockito.verify(mOkHttpClient).newCall(requestCaptor.capture());
        Request req = requestCaptor.getValue();
        assertThat(req).isNotNull();
        assertThat(req.url().toString()).isEqualTo("https://www.google-analytics.com/mp/collect?api_secret=XyzAppSecretZyX&measurement_id=G-1234ABCD");
        assertThat(req.method()).isEqualTo("POST");
        Buffer bodyBuffer = new Buffer();
        //noinspection ConstantConditions
        req.body().writeTo(bodyBuffer);
        assertThat(bodyBuffer.readUtf8()).isEqualTo(
                "{'timestamp_micros':2000000,'client_id':'987654321'," +
                        "'events':[{'name':'ACT','params':{'items':[]," +
                        "'event_category':'CAT','event_label':'LAB'," +
                        "'date_sec':'19010203134243','date_min':'190102031342'," +
                        "'value':72,'currency':'USD'}}]}");
    }
}
