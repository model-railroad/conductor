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

public class JsonSenderTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock private ILogger mLogger;
    @Mock private OkHttpClient mOkHttpClient;
    @Mock private ScheduledExecutorService mExecutor;

    private final FakeClock mFakeClock = new FakeClock(1000);
    private final FileOps mFileOps = new FakeFileOps();
    private JsonSender mSender;

    @Before
    public void setUp() throws Exception {
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

        mSender = new JsonSender(mLogger, mFileOps, mFakeClock, mOkHttpClient, df, mExecutor);
    }

    @After
    public void tearDown() throws Exception {
        mSender.shutdown();
    }

    @Test
    public void testSetJsonUrl_FromString() throws Exception {
        assertThat(mSender.getJsonUrl()).isNull();

        mSender.setJsonUrl(" http://example.com/some/url # Comment \nBlah");
        assertThat(mSender.getJsonUrl().toString()).isEqualTo("http://example.com/some/url");
    }

    @Test
    public void testSetJsonUrl_FromFile() throws Exception {
        assertThat(mSender.getJsonUrl()).isNull();

        mFileOps.writeBytes(
                " http://example.com/some/url # Comment \n Blah".getBytes(Charsets.UTF_8),
                new File("/tmp/id.txt"));

        mSender.setJsonUrl("@/tmp/id.txt");
        assertThat(mSender.getJsonUrl().toString()).isEqualTo("http://example.com/some/url");
    }

    @Test
    public void testValidateJsonFormat() throws Exception {
        mSender.sendEvent("computer", null, "on");
        mFakeClock.sleep(1);
        mSender.sendEvent("conductor", null, "off");
        mFakeClock.sleep(1);
        mSender.sendEvent("toggles", "passenger", "on");
        mFakeClock.sleep(1);
        mSender.sendEvent("toggles", "branchline", "off");
        mFakeClock.sleep(1);
        mSender.sendEvent("depart", "passenger", null);
        mFakeClock.sleep(1);
        mSender.sendEvent("depart", "freight", null);
        mFakeClock.sleep(1);
        mSender.sendEvent("depart", "branchline", null);

        String json = mSender.toJsonString();
        assertThat(json).isEqualTo(
                "{\n" +
                        "  \"computer\" : {\n" +
                        "    \"ts\" : \"1970-01-01T00:00:01Z\",\n" +
                        "    \"value\" : \"on\"\n" +
                        "  },\n" +
                        "  \"conductor\" : {\n" +
                        "    \"ts\" : \"1970-01-01T00:00:01Z\",\n" +
                        "    \"value\" : \"off\"\n" +
                        "  },\n" +
                        "  \"depart\" : {\n" +
                        "    \"branchline\" : {\n" +
                        "      \"ts\" : \"1970-01-01T00:00:01Z\"\n" +
                        "    },\n" +
                        "    \"freight\" : {\n" +
                        "      \"ts\" : \"1970-01-01T00:00:01Z\"\n" +
                        "    },\n" +
                        "    \"passenger\" : {\n" +
                        "      \"ts\" : \"1970-01-01T00:00:01Z\"\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"toggles\" : {\n" +
                        "    \"branchline\" : {\n" +
                        "      \"ts\" : \"1970-01-01T00:00:01Z\",\n" +
                        "      \"value\" : \"off\"\n" +
                        "    },\n" +
                        "    \"passenger\" : {\n" +
                        "      \"ts\" : \"1970-01-01T00:00:01Z\",\n" +
                        "      \"value\" : \"on\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}");
    }

    @Test
    public void testSendNoUrl() throws Exception {
        mSender.sendEvent("key1", null, null);
        // our mock executor executes immediately
        verifyZeroInteractions(mOkHttpClient);
    }

    @Test
    public void testSendNoData() throws Exception {
        mSender.setJsonUrl("http://example.com/url");
        mSender.sendEvent(null, null, null);
        // our mock executor executes immediately
        verifyZeroInteractions(mOkHttpClient);
    }

    @Test
    public void testSendDataSuccess() throws Exception {
        String url = "https://example.com/url";
        Call call = mock(Call.class);
        when(mOkHttpClient.newCall(ArgumentMatchers.any())).thenReturn(call);
        Response response = new Response.Builder()
                .code(200)
                .request(new Request.Builder().url(url).build())
                .protocol(Protocol.HTTP_1_1)
                .message("Message")
                .build();
        when(call.execute()).thenReturn(response);

        mSender.setJsonUrl(url);
        mSender.sendEvent("key1", null, null);
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
        assertThat(bodyBuffer.readUtf8()).contains("key1");

        verifyNoMoreInteractions(mOkHttpClient);
    }

    @Test
    public void testSendDataRetry() throws Exception {
        String url = "https://example.com/url";
        Call call = mock(Call.class);
        when(mOkHttpClient.newCall(ArgumentMatchers.any())).thenReturn(call);
        Response badResponse = new Response.Builder()
                .code(418)
                .request(new Request.Builder().url(url).build())
                .protocol(Protocol.HTTP_1_1)
                .message("Message")
                .build();
        Response goodResponse = new Response.Builder()
                .code(200)
                .request(new Request.Builder().url(url).build())
                .protocol(Protocol.HTTP_1_1)
                .message("Message")
                .build();
        when(call.execute())
                .thenReturn(badResponse)
                .thenReturn(goodResponse);

        mSender.setJsonUrl(url);
        mSender.sendEvent("key1", null, null);
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
            assertWithMessage("req %s", index).that(bodyBuffer.readUtf8()).contains("key1");
        }
    }
}
