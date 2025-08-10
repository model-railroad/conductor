/*
 * Project: Conductor
 * Copyright (C) 2025 alf.labs gmail com,
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
import com.alflabs.utils.FakeFileOps;
import com.alflabs.utils.FileOps;
import com.alflabs.utils.ILogger;
import com.google.common.base.Charsets;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class DazzSenderTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock private ILogger mLogger;
    @Mock private OkHttpClient mOkHttpClient;
    @Mock private ScheduledExecutorService mExecutor;

    private final FakeClock mFakeClock = new FakeClock(1000);
    private final FileOps mFileOps = new FakeFileOps();
    private DazzSender mSender;

    @Before
    public void setUp() {
        // Mock the executor so that it executes all tasks immediately
        when(mExecutor.schedule(ArgumentMatchers.any(Runnable.class), ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class)))
                .thenAnswer((Answer<ScheduledFuture<?>>) invocation -> {
                    ((Runnable) invocation.getArgument(0)).run();
                    return null;
                });
        doAnswer((Answer<ScheduledFuture<?>>) invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(mExecutor).execute(ArgumentMatchers.any(Runnable.class));

        // Format timestamps using ISO 8601, with a fixed UTC timezone to avoid unit test failures.
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        mSender = new DazzSender(mLogger, mFileOps, mFakeClock, mOkHttpClient, df, mExecutor);
    }

    @After
    public void tearDown() throws Exception {
        mSender.shutdown();
    }

    @Test
    public void testSetDazzUrl_FromString() throws Exception {
        assertThat(mSender.getDazzUrl()).isNull();

        mSender.setDazzUrl(" http://example.com/store # Comment \nBlah");
        assertThat(mSender.getDazzUrl().toString()).isEqualTo("http://example.com/store");
    }

    @Test
    public void testSetDazzUrl_FromFile() throws Exception {
        assertThat(mSender.getDazzUrl()).isNull();

        mFileOps.writeBytes(
                " http://example.com/store # Comment \n Blah".getBytes(Charsets.UTF_8),
                new File("/tmp/id.txt"));

        mSender.setDazzUrl("@/tmp/id.txt");
        assertThat(mSender.getDazzUrl().toString()).isEqualTo("http://example.com/store");
    }

    @Test
    public void testSendNoUrl() throws Exception {
        mSender.sendEvent("key1/key2", /*timestampMs=*/ 1000, /*state=*/ true, "payload");
        // our mock executor executes immediately
        verifyZeroInteractions(mOkHttpClient);
    }

    @Test
    public void testSendNoData() throws Exception {
        mSender.setDazzUrl("http://example.com/store");
        mSender.sendEvent(/*key missing=*/"", /*timestampMs=*/ 1000, /*state=*/ false);
        // our mock executor executes immediately
        verifyZeroInteractions(mOkHttpClient);
    }

    @Test
    public void testSendDataSuccess() throws Exception {
        String url = "https://example.com/store";
        Call call = mock(Call.class);
        when(mOkHttpClient.newCall(ArgumentMatchers.any())).thenReturn(call);
        Response response = new Response.Builder()
                .code(200)
                .request(new Request.Builder().url(url).build())
                .protocol(Protocol.HTTP_1_1)
                .message("Message")
                .build();
        when(call.execute()).thenReturn(response);

        mSender.setDazzUrl(url);
        mSender.sendEvent("key1/key2", /*timestampMs=*/ 1000, /*state=*/ true, "payload");
        // our mock executor executes immediately

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mOkHttpClient).newCall(requestCaptor.capture());
        verify(call).execute();

        Request req = requestCaptor.getValue();
        assertThat(req).isNotNull();
        assertThat(req.url().toString()).isEqualTo(url);
        assertThat(req.method()).isEqualTo("POST");
        Buffer bodyBuffer = new Buffer();
        //noinspection ConstantConditions
        req.body().writeTo(bodyBuffer);
        assertThat(bodyBuffer.readUtf8()).isEqualTo(
                "{\"key\":\"key1/key2\",\"ts\":\"1970-01-01T00:00:01Z\",\"st\":true,\"d\":\"payload\"}"
        );

        verifyNoMoreInteractions(mOkHttpClient);
    }

    @Test
    public void testSendDataRetry() throws Exception {
        String url = "https://example.com/store";
        Call call = mock(Call.class);
        when(mOkHttpClient.newCall(ArgumentMatchers.any())).thenReturn(call);
        Response badResponse = new Response.Builder()
                .code(HttpURLConnection.HTTP_CLIENT_TIMEOUT)
                .request(new Request.Builder().url(url).build())
                .protocol(Protocol.HTTP_1_1)
                .message("Message")
                .build();
        Response goodResponse = new Response.Builder()
                .code(HttpURLConnection.HTTP_OK)
                .request(new Request.Builder().url(url).build())
                .protocol(Protocol.HTTP_1_1)
                .message("Message")
                .build();
        when(call.execute())
                .thenReturn(badResponse)
                .thenReturn(goodResponse);

        mSender.setDazzUrl(url);
        mSender.sendEvent("key1/key2", /*timestampMs=*/ 1000, /*state=*/ true, "payload");
        // our mock executor executes immediately (both expected calls are done right here).

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mOkHttpClient, times(2)).newCall(requestCaptor.capture());
        verify(call, times(2)).execute();

        int index = 0;
        for (Request req : requestCaptor.getAllValues()) {
            index++;
            assertWithMessage("req %s", index).that(req).isNotNull();
            assertWithMessage("req %s", index).that(req.url().toString()).isEqualTo(url);
            assertWithMessage("req %s", index).that(req.method()).isEqualTo("POST");
            Buffer bodyBuffer = new Buffer();
            //noinspection ConstantConditions
            req.body().writeTo(bodyBuffer);
            assertWithMessage("req %s", index).that(bodyBuffer.readUtf8()).isEqualTo(
                    "{\"key\":\"key1/key2\",\"ts\":\"1970-01-01T00:00:01Z\",\"st\":true,\"d\":\"payload\"}"
            );
        }
    }

    @Test
    public void testSendMultipleEvents() throws Exception {
        String url = "https://example.com/store";
        Call call = mock(Call.class);
        when(mOkHttpClient.newCall(ArgumentMatchers.any())).thenReturn(call);
        Response response = new Response.Builder()
                .code(200)
                .request(new Request.Builder().url(url).build())
                .protocol(Protocol.HTTP_1_1)
                .message("Message")
                .build();
        when(call.execute()).thenReturn(response);

        mSender.setDazzUrl(url);

        mSender.sendEvent("conductor",                                 /*state=*/ false);
        mSender.sendEvent("computer/consist",   /*timestampMs=*/ 2000, /*state=*/ true);
        mSender.sendEvent("toggle/passenger",   /*timestampMs=*/ 3000, /*state=*/ true);
        mSender.sendEvent("toggle/branchline",  /*timestampMs=*/ 4000, /*state=*/ false);
        mSender.sendEvent("route/passenger",    /*timestampMs=*/ 5000, /*state=*/ true, "payload 1");
        mSender.sendEvent("route/freight",      /*timestampMs=*/ 6000, /*state=*/ true, "payload 2");
        mSender.sendEvent("route/branchline",   /*timestampMs=*/ 7000, /*state=*/ true, "payload 3");

        // our mock executor executes immediately (all expected calls are done right here).
        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);
        verify(mOkHttpClient, times(7)).newCall(requestCaptor.capture());
        verify(call, times(7)).execute();

        StringBuilder responses = new StringBuilder();
        int index = 0;
        for (Request req : requestCaptor.getAllValues()) {
            index++;
            assertWithMessage("req %s", index).that(req).isNotNull();
            assertWithMessage("req %s", index).that(req.url().toString()).isEqualTo(url);
            assertWithMessage("req %s", index).that(req.method()).isEqualTo("POST");
            Buffer bodyBuffer = new Buffer();
            //noinspection ConstantConditions
            req.body().writeTo(bodyBuffer);
            responses
                    .append("req ").append(index).append(": ")
                    .append(bodyBuffer.readUtf8())
                    .append('\n');
        }
        assertThat(responses.toString()).isEqualTo(
                "req 1: {\"key\":\"conductor\",\"ts\":\"1970-01-01T00:00:01Z\",\"st\":false,\"d\":\"\"}\n" +
                "req 2: {\"key\":\"computer/consist\",\"ts\":\"1970-01-01T00:00:02Z\",\"st\":true,\"d\":\"\"}\n" +
                "req 3: {\"key\":\"toggle/passenger\",\"ts\":\"1970-01-01T00:00:03Z\",\"st\":true,\"d\":\"\"}\n" +
                "req 4: {\"key\":\"toggle/branchline\",\"ts\":\"1970-01-01T00:00:04Z\",\"st\":false,\"d\":\"\"}\n" +
                "req 5: {\"key\":\"route/passenger\",\"ts\":\"1970-01-01T00:00:05Z\",\"st\":true,\"d\":\"payload 1\"}\n" +
                "req 6: {\"key\":\"route/freight\",\"ts\":\"1970-01-01T00:00:06Z\",\"st\":true,\"d\":\"payload 2\"}\n" +
                "req 7: {\"key\":\"route/branchline\",\"ts\":\"1970-01-01T00:00:07Z\",\"st\":true,\"d\":\"payload 3\"}\n"
        );
    }
}
