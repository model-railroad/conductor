/*
 * Project: Conductor
 * Copyright (C) 2019 alf.labs gmail com,
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

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.utils.FileOps;
import com.alflabs.utils.IClock;
import com.alflabs.utils.ILogger;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TreeMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Sends JSON status for the Wazz dashboard.
 * <br/>
 * The status data is essentially a key-value map, with the specificity that
 * it's really "key1 / key2 -> { timestamp + status payload }".
 * Timestamps is an ISO 8601 string.
 * Ststus payload is a string.
 * <br/>
 * The receiver maintains two dictionaries, one for the outer "key1" with an inner one for "key2"
 * and simply overwrites the latest status entry with the timestamp/payload.
 * <br/>
 * One peculiarity of this sender is that we accumulate all keys on this side and then send
 * them all every time, instead of just the last one provided by 'sendEvent'. The original
 * design goal was that, if we miss sending one event, the next one will have the accumulation
 * of all previous statuses.
 * <br/>
 * The executor has an exponential retry delay and keeps trying to send the last message on
 * failure. Since each message is an accumulation of everything sent before, a new event
 * resets the retry count/delay and the message to send.
 * <br/>
 * One drawback is that as the program runs, the message sent increases as it accumulates all
 * previous key entries. However, this is bounded: we only accumulate the last entry for every key,
 * the number of keys is bounded in the Conductor script (about 4~6 of them), and the Conductor
 * program is always running in a limited 10AM-5PM time window at best. The design is thus sound
 * for this particular application, and not made to be generic.
 * <br/>
 * See "engine2k/src/test/.../v2/script/ScriptTest3Test2k.kt" for an example of real payloads.
 */
public class JsonSender implements Runnable {
    private static final String TAG = JsonSender.class.getSimpleName();

    private static final boolean DEBUG = false;
    private static final MediaType sMediaType = MediaType.parse("text/plain");

    private final ILogger mLogger;
    private final FileOps mFileOps;
    private final IClock mClock;
    // Format timestamps using ISO 8601, e.g.:
    // DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final DateFormat mJsonDateFormat;
    private final OkHttpClient mOkHttpClient;
    // Note: The executor is a dagger singleton, shared with the Analytics class.
    private final ScheduledExecutorService mExecutor;
    private final TreeMap<String, Object> mKeyValues = new TreeMap<>();
    @VisibleForTesting
    protected final AtomicReference<String> mLatestJson = new AtomicReference<>();

    private long mRetryDelay;
    @Null private HttpUrl mJsonUrl;

    @Inject
    public JsonSender(ILogger logger,
                      FileOps fileOps,
                      IClock clock,
                      OkHttpClient okHttpClient,
                      @Named("IsoUtcDateTime") DateFormat isoUtcDateFormat,
                      @Named("SingleThreadExecutor") ScheduledExecutorService executor) {
        mLogger = logger;
        mFileOps = fileOps;
        mClock = clock;
        mOkHttpClient = okHttpClient;
        mJsonDateFormat = isoUtcDateFormat;
        mExecutor = executor;
    }

    /**
     * Requests termination. Pending tasks will be executed, no new task is allowed.
     * Waiting time is 10 seconds max.
     * <p/>
     * Side effect: The executor is now a dagger singleton. This affects other classes that
     * use the same executor, e.g. {@link Analytics}.
     */
    public void shutdown() throws InterruptedException {
        mExecutor.shutdown();
        mExecutor.awaitTermination(10, TimeUnit.SECONDS);
        mLogger.d(TAG, "Shutdown");
    }

    public void setJsonUrl(String urlOrFile) throws IOException {
        if (urlOrFile.startsWith("\"") && urlOrFile.endsWith("\"") && urlOrFile.length() > 2) {
            urlOrFile = urlOrFile.substring(1, urlOrFile.length() - 1);
        }

        if (urlOrFile.startsWith("@")) {
            urlOrFile = urlOrFile.substring(1);
            File file = new File(urlOrFile);
            if (urlOrFile.startsWith("~") && !mFileOps.isFile(file)) {
                file = new File(System.getProperty("user.home"), urlOrFile.substring(1));
            }
            urlOrFile = mFileOps.toString(file, Charsets.UTF_8);
        }

        // Use "#" as a comment and only take the first thing before, if any.
        urlOrFile = urlOrFile.replaceAll("[#\n\r].*", "").trim();
        // Somewhat sanitize the URL
        urlOrFile = urlOrFile.replaceAll("[^a-zA-Z0-9@:/._+-]", "");

        mJsonUrl = HttpUrl.parse(urlOrFile);
        mLogger.d(TAG, "JSON Sender URL: " + mJsonUrl);

        if (mLatestJson.get() != null) {
            mRetryDelay = 0;
            scheduleSend();
        }
    }

    @Null
    public HttpUrl getJsonUrl() {
        return mJsonUrl;
    }

    public void sendEvent(@NonNull String key1, @Null String key2, @Null String value) {
        if (key1 == null || key1.isEmpty()) {
            mLogger.d(TAG, "JSON Sender: Ignoring event with no key1");
            return;
        }

        Entry entry = new Entry(value, mClock.elapsedRealtime());

        key1 = key1.toLowerCase(Locale.US);
        if (key2 == null || key2.isEmpty()) {
            mKeyValues.put(key1, entry);

        } else {
            Object o1 = mKeyValues.get(key1);
            TreeMap<String, Entry> map = (o1 instanceof TreeMap) ? (TreeMap) o1 : null;
            if (map == null) {
                map = new TreeMap<>();
                mKeyValues.put(key1, map);
            }
            key2 = key2.toLowerCase(Locale.US);
            map.put(key2, entry);
        }

        try {
            mLatestJson.set(toJsonString());
            mRetryDelay = 0;
            scheduleSend();
        } catch (JsonProcessingException e) {
            mLogger.d(TAG, "JSON Sender: Error creating JSON entry", e);
        } catch (Exception e) {
            mLogger.d(TAG, "JSON Sender: Unexpected Error", e);
        }
    }

    @VisibleForTesting
    protected void scheduleSend() {
        mExecutor.execute(this);
    }

    public String toJsonString() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        // Pretty print makes it easier to debug & for unit tests.
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // Sorting technically not needed due to already using a sorted TreeMap.
        mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        // We don't want the order of serialized objects to change (for unit tests).
        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        // Remove null values in entries.
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        // Automatically format Date types.
        mapper.setDateFormat(mJsonDateFormat);
        return mapper.writeValueAsString(mKeyValues).replaceAll("\r", "");
    }

    @Override
    public void run() {
        String jsonData = mLatestJson.getAndSet(null);
        if (jsonData == null) {
            mLogger.d(TAG, "JSON Sender: No data to send");
            return;
        }

        if (mJsonUrl == null) {
            mLogger.d(TAG, "JSON Sender: URL not set yet on queued payload.");
            return;
        }

        OkHttpClient client = mOkHttpClient;
        HttpUrl url = mJsonUrl;
        String usr = url.encodedUsername();
        String pwd = url.encodedPassword();
        if (!usr.isEmpty() && !pwd.isEmpty()) {
            url = url.newBuilder().username("").password("").build();
            Authenticator auth = new Authenticator() {
                @Null
                @Override
                public Request authenticate(Route route, Response response) throws IOException {
                    mLogger.d(TAG, "JSON Sender: HTTP Auth for " + response);
                    String basic = Credentials.basic(usr, pwd);
                    return response.request().newBuilder().header("Authorization", basic).build();
                }
            };
            client = client.newBuilder().authenticator(auth).build();
        }

        Request.Builder builder = new Request.Builder().url(url);
        RequestBody body = RequestBody.create(sMediaType, jsonData);
        builder.post(body);

        Request request = builder.build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            mLogger.d(TAG, "JSON Sender: HTTP Response " + response);
            if (response != null) {
                if (response.isSuccessful()) {
                    return;
                }
            }
        } catch (IOException e) {
            mLogger.d(TAG, "JSON Sender: Error sending JSON", e);
        } finally {
            if (response != null && response.body() != null) {
                response.body().close();
            }
        }

        if (mLatestJson.get() == null && !mExecutor.isShutdown()) {
            mRetryDelay = Math.min(30*60, 5 + 2 * mRetryDelay);
            mLogger.d(TAG, "JSON Sender: Will retry in " + mRetryDelay + " seconds");
            mLatestJson.set(jsonData);
            mExecutor.schedule(this, mRetryDelay, TimeUnit.SECONDS);
        }
    }

    private static class Entry {
        private final String mValue;
        private final Date mTs;

        public Entry(@Null String value, long ts) {
            mValue = value;
            mTs = new Date(ts);
        }

        @Null
        public String getValue() {
            return mValue;
        }

        @NonNull
        public Date getTs() {
            return mTs;
        }
    }
}
