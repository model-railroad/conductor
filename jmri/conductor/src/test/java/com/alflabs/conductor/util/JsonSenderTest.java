package com.alflabs.conductor.util;

import com.alflabs.utils.FakeClock;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static com.google.common.truth.Truth.assertThat;

public class JsonSenderTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock private ILogger mLogger;
    @Mock private OkHttpClient mOkHttpClient;

    private FakeClock mFakeClock = new FakeClock(1000);
    private FileOps mFileOps = new FakeFileOps();
    private JsonSender mSender;

    @Before
    public void setUp() throws Exception {
        // Format timestamps using ISO 8601, with a fixed UTC timezone to avoid unit test failures.
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        mSender = new JsonSender(mLogger, mFileOps, mFakeClock, df, mOkHttpClient);
    }

    @Test
    public void testSetJsonUrl_FromString() throws Exception {
        assertThat(mSender.getJsonUrl()).isNull();

        mSender.setJsonUrl(" http://example.com/some/url # Comment \nBlah");
        assertThat(mSender.getJsonUrl()).isEqualTo("http://example.com/some/url");
    }

    @Test
    public void testSetJsonUrl_FromFile() throws Exception {
        assertThat(mSender.getJsonUrl()).isNull();

        mFileOps.writeBytes(
                " http://example.com/some/url # Comment \n Blah".getBytes(Charsets.UTF_8),
                new File("/tmp/id.txt"));

        mSender.setJsonUrl("@/tmp/id.txt");
        assertThat(mSender.getJsonUrl()).isEqualTo("http://example.com/some/url");
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
                        "    \"value\" : \"on\",\n" +
                        "    \"ts\" : \"1970-01-01T00:00:01Z\"\n" +
                        "  },\n" +
                        "  \"conductor\" : {\n" +
                        "    \"value\" : \"off\",\n" +
                        "    \"ts\" : \"1970-01-01T00:00:01Z\"\n" +
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
                        "      \"value\" : \"off\",\n" +
                        "      \"ts\" : \"1970-01-01T00:00:01Z\"\n" +
                        "    },\n" +
                        "    \"passenger\" : {\n" +
                        "      \"value\" : \"on\",\n" +
                        "      \"ts\" : \"1970-01-01T00:00:01Z\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}");
    }
}
