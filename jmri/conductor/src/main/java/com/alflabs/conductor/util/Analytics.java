package com.alflabs.conductor.util;

import com.alflabs.annotations.NonNull;
import com.alflabs.kv.IKeyValue;
import com.alflabs.manifest.Constants;
import com.alflabs.utils.FileOps;
import com.alflabs.utils.ILogger;
import com.google.common.base.Charsets;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Analytics {
    private static final String TAG = Analytics.class.getSimpleName();

    private static final boolean DEBUG = false;
    private static final boolean USE_GET = false; // default is POST

    private static final String GA_URL =
            "https://www.google-analytics.com/"
            + (DEBUG ? "debug/" : "")
            + "collect";

    private static final String UTF_8 = "UTF-8";
    private static final MediaType sMediaType = MediaType.parse("text/plain");

    private final ILogger mLogger;
    private final FileOps mFileOps;
    private final IKeyValue mKeyValue;
    private final Random mRandom;
    private final OkHttpClient mOkHttpClient;
    // Note: The executor is a dagger singleton, shared with the JsonSender.
    private final ScheduledExecutorService mExecutor;

    private String mTrackingId = null;

    @Inject
    public Analytics(ILogger logger,
                     FileOps fileOps,
                     IKeyValue keyValue,
                     OkHttpClient okHttpClient,
                     Random random,
                     @Named("SingleThreadExecutor") ScheduledExecutorService executor) {
        mLogger = logger;
        mFileOps = fileOps;
        mKeyValue = keyValue;
        mRandom = random;
        mOkHttpClient = okHttpClient;
        mExecutor = executor;
    }

    /**
     * Requests termination. Pending tasks will be executed, no new task is allowed.
     * Waiting time is 10 seconds max.
     * <p/>
     * Side effect: The executor is now a dagger singleton. This affects other classes that
     * use the same executor, e.g. {@link JsonSender}.
     */
    public void shutdown() throws InterruptedException {
        mExecutor.shutdown();
        mExecutor.awaitTermination(10, TimeUnit.SECONDS);
        mLogger.d(TAG, "Shutdown");
    }

    public void setTrackingId(@NonNull String idOrFile) throws IOException {
        if (idOrFile.startsWith("\"") && idOrFile.endsWith("\"") && idOrFile.length() > 2) {
            idOrFile = idOrFile.substring(1, idOrFile.length() - 1);
        }

        if (idOrFile.startsWith("@")) {
            idOrFile = idOrFile.substring(1);
            File file = new File(idOrFile);
            if (idOrFile.startsWith("~") && !mFileOps.isFile(file)) {
                file = new File(System.getProperty("user.home"), idOrFile.substring(1));
            }
            idOrFile = mFileOps.toString(file, Charsets.UTF_8);
        }
        // Use "#" as a comment and only take the first thing before, if any.
        idOrFile = idOrFile.replaceAll("[#\n\r].*", "");
        // GA Id format is "UA-Numbers-1" so accept only letters, numbers, hyphen. Ignore the rest.
        idOrFile = idOrFile.replaceAll("[^A-Z0-9-]", "");

        mTrackingId = idOrFile;
        mKeyValue.putValue(Constants.GAId, idOrFile, true /*broadcast*/);
        mLogger.d(TAG, "Tracking ID: " + mTrackingId);
    }

    public String getTrackingId() {
        return mTrackingId;
    }

    public void sendEvent(
            @NonNull String category,
            @NonNull String action,
            @NonNull String label,
            @NonNull String user_) {
        if (mTrackingId == null || mTrackingId.isEmpty()) {
            mLogger.d(TAG, "No Tracking ID");
            return;
        }

        mExecutor.execute(() -> {
            try {
                int random = mRandom.nextInt();
                if (random < 0) {
                    random = -random;
                }

                String user = user_;
                if (user.length() > 0 && Character.isDigit(user.charAt(0))) {
                    user = "user" + user;
                }

                String cid = UUID.nameUUIDFromBytes(user.getBytes()).toString();

                String payload = String.format(
                        "v=1&tid=%s&ds=consist&cid=%s&t=event&ec=%s&ea=%s&el=%s&z=%d",
                        URLEncoder.encode(mTrackingId, UTF_8),
                        URLEncoder.encode(cid, UTF_8),
                        URLEncoder.encode(category, UTF_8),
                        URLEncoder.encode(action, UTF_8),
                        URLEncoder.encode(label, UTF_8),
                        random);

                Response response = sendPayload(payload);

                mLogger.d(TAG, String.format("Event [%s %s %s %s] code: %d",
                        category, action, label, user, response.code()));

                if (DEBUG) {
                    mLogger.d(TAG, "Event body: " + response.body().string());
                }

                response.close();

            } catch (Exception e) {
                mLogger.d(TAG, "Event ERROR: " + e);
            }
        });
    }

    public void sendPage(
            @NonNull String url_,
            @NonNull String path,
            @NonNull String user_) {
        if (mTrackingId == null) {
            mLogger.d(TAG, "No Tracking ID");
            return;
        }

        mExecutor.execute(() -> {
            try {
                int random = mRandom.nextInt();
                if (random < 0) {
                    random = -random;
                }

                String user = user_;
                if (user.length() > 0 && Character.isDigit(user.charAt(0))) {
                    user = "user" + user;
                }

                String cid = UUID.nameUUIDFromBytes(user.getBytes()).toString();

                String d_url = url_ + path;

                String payload = String.format(
                        "v=1&tid=%s&ds=consist&cid=%s&t=pageview&dl=%s&z=%d",
                        URLEncoder.encode(mTrackingId, UTF_8),
                        URLEncoder.encode(cid, UTF_8),
                        URLEncoder.encode(d_url, UTF_8),
                        random);

                Response response = sendPayload(payload);

                mLogger.d(TAG, String.format("PageView [%s %s] code: %d",
                        d_url, user, response.code()));

                if (DEBUG) {
                    mLogger.d(TAG, "Event body: " + response.body().string());
                }

                response.close();

            } catch (Exception e) {
                mLogger.d(TAG, "Page ERROR: " + e);
            }
        });
    }

    // Must be executed in background thread. Caller must call Response.close().
    private Response sendPayload(String payload) throws IOException {
        if (DEBUG) {
            mLogger.d(TAG, "Event Payload: " + payload);
        }

        String url = GA_URL;
        if (USE_GET) {
            url += "?" + payload;
        }

        Request.Builder builder = new Request.Builder().url(url);

        if (!USE_GET) {
            RequestBody body = RequestBody.create(sMediaType, payload);
            builder.post(body);
        }

        Request request = builder.build();
        return mOkHttpClient.newCall(request).execute();
    }
}
