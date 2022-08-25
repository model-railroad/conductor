package com.alflabs.rtac.service;

import android.util.Log;
import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.kv.IKeyValue;
import com.alflabs.kv.KeyValueClient;
import com.alflabs.manifest.Constants;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rx.AndroidSchedulers;
import com.alflabs.rx.IStream;
import com.alflabs.rx.ISubscriber;
import com.alflabs.utils.ServiceMixin;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class AnalyticsMixin extends ServiceMixin<RtacService> {
    private static final String TAG = AnalyticsMixin.class.getSimpleName();

    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final boolean DEBUG_VERBOSE = false;
    private static final boolean USE_GET = false; // default is POST

    private static final String GA_URL =
            "https://www.google-analytics.com/"
            + (DEBUG_VERBOSE ? "debug/" : "")
            + "collect";

    private static final String UTF_8 = "UTF-8";
    private static final Random sRandom = new Random();
    private static final MediaType sMediaType = MediaType.parse("text/plain");

    private final DataClientMixin mDataClientMixin;

    private ExecutorService mExecutorService;
    private volatile String mTrackingId = null;
    private volatile CountDownLatch mTrackingLatch = null;

    @Inject
    public AnalyticsMixin(DataClientMixin dataClientMixin) {
        mDataClientMixin = dataClientMixin;
    }

    @Override
    public void onCreate(RtacService service) {
        super.onCreate(service);
        if (DEBUG) Log.d(TAG, "onCreate");
        mDataClientMixin.getKeyChangedStream().subscribe(AndroidSchedulers.mainThread(), mKeyChangedSubscriber);
        if (mTrackingId == null) {
            mTrackingLatch = new CountDownLatch(1);
        }
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) Log.d(TAG, "onDestroy");
        mDataClientMixin.getKeyChangedStream().remove(mKeyChangedSubscriber);
        if (mTrackingLatch != null) {
            mTrackingLatch.countDown();
            mTrackingLatch = null;
        }
        mExecutorService.shutdown();
        mExecutorService = null;
    }

    private final ISubscriber<String> mKeyChangedSubscriber = new ISubscriber<String>() {
        @Override
        public void onReceive(IStream<? extends String> stream, String key) {
            if (Constants.GAId.equals(key)) {
                IKeyValue kvClient = mDataClientMixin.getKeyValueClient();
                if (kvClient == null) return;
                mTrackingId = kvClient.getValue(key);
                if (DEBUG) Log.d(TAG, "TrackingId = " + mTrackingId);
                if (mTrackingLatch != null) {
                    mTrackingLatch.countDown();
                }
            }
        }
    };

    public void sendEvent(
            @NonNull String category,
            @NonNull String action,
            @NonNull String label,
            @NonNull String user_) {
        sendEvent(category, action, label, user_, null);
    }

    public void sendEvent(
            @NonNull String category,
            @NonNull String action,
            @NonNull String label,
            @NonNull String user_,
            @Null Integer value) {
        mExecutorService.execute(() -> {
            try {
                if (mTrackingLatch != null) {
                    if (DEBUG) Log.d(TAG, "sendEvent wait for tracking id");
                    mTrackingLatch.await();
                    mTrackingLatch = null;
                }

                if (mTrackingId == null) {
                    if (DEBUG) Log.d(TAG, "No Tracking ID");
                    return;
                }

                int random = sRandom.nextInt();
                if (random < 0) {
                    random = -random;
                }

                String user = user_;
                if (user.length() > 0 && Character.isDigit(user.charAt(0))) {
                    user = "rtac" + user;
                }

                String cid = UUID.nameUUIDFromBytes(user.getBytes()).toString();

                String payload = String.format(Locale.US,
                        "v=1&tid=%s&ds=consist&cid=%s&t=event&ec=%s&ea=%s&el=%s&z=%d",
                        URLEncoder.encode(mTrackingId, UTF_8),
                        URLEncoder.encode(cid, UTF_8),
                        URLEncoder.encode(category, UTF_8),
                        URLEncoder.encode(action, UTF_8),
                        URLEncoder.encode(label, UTF_8),
                        random);

                if (value != null) {
                    payload = String.format(Locale.US, "%s&ev=%s",
                            payload,
                            URLEncoder.encode(value.toString(), UTF_8));
                }

                Response response = sendPayload(payload);

                if (DEBUG_VERBOSE) Log.d(TAG, String.format("Event [%s %s %s %s] code: %d",
                        category, action, label, user, response.code()));

                if (DEBUG_VERBOSE) Log.d(TAG, "Event body: " + response.body().string());

                response.close();

            } catch (Exception e) {
                if (DEBUG) Log.d(TAG, "Event ERROR: " + e);
            }
        });
    }

    public void sendPage(
            @NonNull String url_,
            @NonNull String path,
            @NonNull String user_) {

        mExecutorService.execute(() -> {
            try {
                if (mTrackingLatch != null) {
                    if (DEBUG) Log.d(TAG, "sendPage wait for tracking id");
                    mTrackingLatch.await();
                    mTrackingLatch = null;
                }

                if (mTrackingId == null) {
                    if (DEBUG) Log.d(TAG, "No Tracking ID");
                    return;
                }

                int random = sRandom.nextInt();
                if (random < 0) {
                    random = -random;
                }

                String user = user_;
                if (user.length() > 0 && Character.isDigit(user.charAt(0))) {
                    user = "user" + user;
                }

                String cid = UUID.nameUUIDFromBytes(user.getBytes()).toString();

                String d_url = url_ + path;

                String payload = String.format(Locale.US,
                        "v=1&tid=%s&ds=consist&cid=%s&t=pageview&dl=%s&z=%d",
                        URLEncoder.encode(mTrackingId, UTF_8),
                        URLEncoder.encode(cid, UTF_8),
                        URLEncoder.encode(d_url, UTF_8),
                        random);

                Response response = sendPayload(payload);

                if (DEBUG_VERBOSE) Log.d(TAG, String.format("PageView [%s %s] code: %d",
                        d_url, user, response.code()));

                if (DEBUG_VERBOSE) Log.d(TAG, "Event body: " + response.body().string());

                response.close();

            } catch (Exception e) {
                if (DEBUG) Log.d(TAG, "Page ERROR: " + e);
            }
        });
    }

    // Must be executed in background thread. Caller must call Response.close().
    private Response sendPayload(String payload) throws IOException {
        if (DEBUG_VERBOSE) Log.d(TAG, "Event Payload: " + payload);

        String url = GA_URL;
        if (USE_GET) {
            url += "?" + payload;
        }

        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder().url(url);

        if (!USE_GET) {
            RequestBody body = RequestBody.create(sMediaType, payload);
            builder.post(body);
        }

        Request request = builder.build();
        return client.newCall(request).execute();
    }
}
