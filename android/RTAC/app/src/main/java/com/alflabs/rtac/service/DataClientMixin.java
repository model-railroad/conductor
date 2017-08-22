package com.alflabs.rtac.service;

import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import com.alflabs.kv.KeyValueClient;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.app.AppPrefsValues;
import com.alflabs.rtac.nsd.DiscoveryListener;
import com.alflabs.rx.IPublisher;
import com.alflabs.rx.IStream;
import com.alflabs.rx.ISubscriber;
import com.alflabs.rx.Publishers;
import com.alflabs.rx.Streams;
import com.alflabs.utils.ILogger;
import com.alflabs.utils.ServiceMixin;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class DataClientMixin extends ServiceMixin<RtacService> {
    private static final String TAG = DataClientMixin.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private final AtomicBoolean mTryToConnect = new AtomicBoolean();
    private final DataClientStatus mStatus = new DataClientStatus();
    private final IStream<DataClientStatus> mStatusStream = Streams.stream();
    private final IPublisher<DataClientStatus> mStatusPublisher = Publishers.latest();

    private KeyValueClient mDataClient;

    @Inject ILogger mLogger;
    @Inject AppPrefsValues mAppPrefsValues;
    @Inject DiscoveryListener mNsdListener;
    @Inject KVClientListener mKVClientListener;

    @Inject
    public DataClientMixin() {
        mStatusStream.publishWith(mStatusPublisher);
    }

    public IStream<DataClientStatus> getStatusStream() {
        return mStatusStream;
    }

    public KeyValueClient getDataClient() {
        return mDataClient;
    }

    @Override
    public void onCreate(RtacService service) {
        super.onCreate(service);
        startCnx();
    }

    @Override
    public void onDestroy() {
        stopCnx();
        super.onDestroy();
    }

    private void startCnx() {
        if (mDataClient == null) {
            setStatus(true, "Data Client: Not Started");
        }

        mTryToConnect.set(true);
        Thread t = new Thread(this::startCnxOnThread, "DataClient-Thread");
        t.start();
    }

    private void startCnxOnThread() {
        if (mDataClient != null) {
            mDataClient.stopSync();
            mDataClient = null;
        }

        ISubscriber<NsdServiceInfo> nsdSubscriber = (stream, serviceInfo) -> onNsdServiceFound(serviceInfo);

        mNsdListener.getServiceResolvedStream().subscribe(nsdSubscriber);

        boolean useNsd = mNsdListener.start();
        if (useNsd) {
            mAppPrefsValues.setData_ServerHostName("");
        }

        connectLoop();

        if (DEBUG) {
            Log.e(TAG, "Data Client Loop: finished");
        }

        mNsdListener.getServiceResolvedStream().remove(nsdSubscriber);

        if (useNsd) {
            mNsdListener.stop();
        }
    }

    private void onNsdServiceFound(NsdServiceInfo serviceInfo) {
        assert serviceInfo != null;
        InetAddress host = serviceInfo.getHost();
        int port = serviceInfo.getPort();
        if (DEBUG) {
            Log.e(TAG, "Data Client Loop: onServiceResolved " + host.getHostAddress() + " port " + port);
        }
        if (host != null && port > 0) {
            mAppPrefsValues.setData_ServerHostName(host.getHostAddress());
            mAppPrefsValues.setData_ServerPort(port);
        }
    }

    private void connectLoop() {
        while (mTryToConnect.get() && mDataClient == null) {
            String dataHostname = mAppPrefsValues.getData_ServerHostName();
            int dataPort = mAppPrefsValues.getData_ServerPort();

            if (DEBUG) {
                Log.e(TAG, "Data Client Loop: connecting to: " + dataHostname + " port " + dataPort);
            }

            try {
                try {
                    if (dataHostname.isEmpty()) throw new UnknownHostException("Empty KV Server HostName");
                    InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(dataHostname), dataPort);
                    mDataClient = new KeyValueClient(mLogger, address, mKVClientListener);
                    // TODO FIXME ++ mDataClient.setOnChangeListener(mActivity);

                    if (mDataClient.startSync()) {
                        mTryToConnect.set(false);
                        mDataClient.requestAllKeys();
                    }

                } catch (UnknownHostException e) {
                    if (DEBUG) {
                        Log.e(TAG, "Data Client Loop: KeyValueClient: ", e);
                    }
                    setStatus(true, "Data Server: Invalid Hostname. Please check settings.");
                }

                if (mTryToConnect.get()) {
                    if (DEBUG) {
                        Log.e(TAG, "Data Client Loop: failed, retry in 2 seconds");
                    }
                    Thread.sleep(2000);
                }
            } catch (InterruptedException e) {
                // data.startSync or thread.sleep got interrupted.
                // if mTryToConnect is false, the while loop will terminate
                // otherwise we'll retry.
            }
        }
    }

    private void setStatus(boolean isError, String message) {
        mStatus.set(isError, message);
        mStatusPublisher.publish(mStatus);
    }

    public void stopCnx() {
        mTryToConnect.set(false);
        if (mDataClient != null) {
            mDataClient.stopAsync();
            mDataClient = null;
            setStatus(false, "Data Server: Disconnected.");
        }
    }

    public static class DataClientStatus {
        private boolean mIsError;
        private String mText;

        void set(boolean isError, String text) {
            mIsError = isError;
            mText = text;
        }

        public boolean isError() {
            return mIsError;
        }

        public String getText() {
            return mText;
        }
    }
}
