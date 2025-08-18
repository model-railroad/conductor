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

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.dazzserv.store.DataEntry;
import com.alflabs.utils.FileOps;
import com.alflabs.utils.IClock;
import com.alflabs.utils.ILogger;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.util.Date;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Sends JSON status to DazzServ.
 */
public class DazzSender implements Runnable {
    private static final String TAG = DazzSender.class.getSimpleName();

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
    @VisibleForTesting
    protected final Deque<DataEntry> mEventQueue = new ConcurrentLinkedDeque<>();

    private long mRetryDelay;
    @Null private HttpUrl mDazzUrl;

    @Inject
    public DazzSender(ILogger logger,
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

    public void setDazzUrl(String urlOrFile) throws IOException {
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

        // Use "#" as a comment and only take the FIRST LINE before, if any.
        urlOrFile = urlOrFile.replaceAll("[#\n\r].*", "").trim();
        // Somewhat sanitize the URL
        urlOrFile = urlOrFile.replaceAll("[^a-zA-Z0-9@:/._+-]", "");

        // Sanity check
        if (!urlOrFile.endsWith("/store")) {
            mLogger.d(TAG, "Dazz Sender WARNING: URL does not end with /store in " + urlOrFile);
        }

        mDazzUrl = HttpUrl.parse(urlOrFile);
        mLogger.d(TAG, "Dazz Sender URL: " + mDazzUrl);

        if (!mEventQueue.isEmpty()) {
            mRetryDelay = 0;
            scheduleSend();
        }
    }

    @Null
    public HttpUrl getDazzUrl() {
        return mDazzUrl;
    }

    public void sendEvent(
            @NonNull String key,
            boolean state) {
        sendEvent(key, mClock.elapsedRealtime(), state, /*payload=*/ null);
    }

    public void sendEvent(
            @NonNull String key,
            boolean state,
            @Null String payload) {
        sendEvent(key, mClock.elapsedRealtime(), state, payload);
    }

    public void sendEvent(
            @NonNull String key,
            long eventTimestampMs,
            boolean state) {
        sendEvent(key, eventTimestampMs, state, /*payload=*/ null);
    }

    public void sendEvent(
            @NonNull String key,
            long eventTimestampMs,
            boolean state,
            @Null String payload) {
        if (key == null || key.isEmpty()) {
            mLogger.d(TAG, "Dazz Sender: Ignoring event with no key");
            return;
        }

        Date date = new Date(eventTimestampMs);
        System.out.println("ts = " + eventTimestampMs + " ==> date = " + date);
        String isoTimestamp = mJsonDateFormat.format(date);
        DataEntry entry = new DataEntry(key, isoTimestamp, state, payload);
        mEventQueue.addLast(entry);
        mRetryDelay = 0;
        scheduleSend();
    }

    @VisibleForTesting
    protected void scheduleSend() {
        mExecutor.execute(this);
    }

    @Override
    public void run() {
        if (mDazzUrl == null) {
            mLogger.d(TAG, "Dazz Sender: URL not set yet on queued event.");
            return;
        }

        DataEntry entry = mEventQueue.peekFirst();
        if (entry == null) {
            mLogger.d(TAG, "Dazz Sender: No data to send");
            return;
        }

        String jsonData;
        try {
            jsonData = entry.toJsonString(/*mapper=*/ null);
        } catch (JsonProcessingException e) {
            mLogger.d(TAG, "Dazz Sender: Discarding, Entry-to-JSON failed.", e);
            mEventQueue.removeFirstOccurrence(entry);
            return;
        }


        OkHttpClient client = mOkHttpClient;
        HttpUrl url = mDazzUrl;
        String usr = url.encodedUsername();
        String pwd = url.encodedPassword();
        if (!usr.isEmpty() && !pwd.isEmpty()) {
            url = url.newBuilder().username("").password("").build();
            Authenticator auth = new Authenticator() {
                @Null
                @Override
                public Request authenticate(Route route, Response response) throws IOException {
                    mLogger.d(TAG, "Dazz Sender: HTTP Auth for " + response);
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
            mLogger.d(TAG, "Dazz Sender: HTTP Response " + response);
            if (response != null) {
                // DazzServ responds to /store with 200 (accepted) or 400 (rejected).
                // A 400 typically indicates the JSON is invalid -- in which case there's no point
                // in retrying sending it, as it would just fail again and again.
                if (response.code() == HttpURLConnection.HTTP_OK ||
                        response.code() == HttpURLConnection.HTTP_BAD_REQUEST) {
                    // Either way, remove the entry from the queue.
                    mEventQueue.removeFirstOccurrence(entry);
                }
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    // If the event queue is not empty, schedule the next send with a little delay
                    if (!mExecutor.isShutdown()) {
                        mRetryDelay = 0;
                        mLogger.d(TAG, "Dazz Sender: Will send next event in 1 second");
                        mExecutor.schedule(this, 1, TimeUnit.SECONDS);
                    }
                    return;
                }
            }
        } catch (IOException e) {
            mLogger.d(TAG, "Dazz Sender: Error sending JSON", e);
        } finally {
            if (response != null && response.body() != null) {
                response.body().close();
            }
        }

        // We get here if the http client above failed (and the entry is still in the queue)
        // or the entry was rejected (and we have more events to send).
        // Just reschedule the executor with a retry delay.
        if (!mEventQueue.isEmpty() && !mExecutor.isShutdown()) {
            mRetryDelay = Math.min(30*60, 5 + 2 * mRetryDelay);
            mLogger.d(TAG, "Dazz Sender: Will retry in " + mRetryDelay + " seconds");
            mExecutor.schedule(this, mRetryDelay, TimeUnit.SECONDS);
        }
    }
}
