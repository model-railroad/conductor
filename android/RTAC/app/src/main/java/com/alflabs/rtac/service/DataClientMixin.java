package com.alflabs.rtac.service;

import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.util.Log;
import com.alflabs.kv.KeyValueClient;
import com.alflabs.manifest.Constants;
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

    private final AtomicBoolean mKeepConnected = new AtomicBoolean();
    private final DataClientStatus mStatus = new DataClientStatus();
    private final IStream<DataClientStatus> mStatusStream = Streams.stream();
    private final IPublisher<DataClientStatus> mStatusPublisher = Publishers.latest();

    private KeyValueClient mDataClient;

    @Inject ILogger mLogger;
    @Inject AppPrefsValues mAppPrefsValues;
    @Inject DiscoveryListener mNsdListener;
    @Inject KVClientListener mKVClientListener;
    @Inject WifiManager mWifiManager;

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

        mKeepConnected.set(true);
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

        if (DEBUG) Log.i(TAG, "Data Client Loop: finished");

        mNsdListener.getServiceResolvedStream().remove(nsdSubscriber);

        if (useNsd) {
            mNsdListener.stop();
        }
    }

    private void onNsdServiceFound(NsdServiceInfo serviceInfo) {
        assert serviceInfo != null;
        InetAddress host = serviceInfo.getHost();
        int port = serviceInfo.getPort();
        if (DEBUG) Log.i(TAG, "Data Client Loop: onNsdServiceFound " + host.getHostAddress() + " port " + port);
        if (host != null && port > 0) {
            mAppPrefsValues.setData_ServerHostName(host.getHostAddress());
            // Note: The port reported by the NSD discovery is for the Withrottle service, not the data server.
            mAppPrefsValues.setData_ServerPort(Constants.KV_SERVER_PORT);
        }
    }

    private void connectLoop() {
        while (mKeepConnected.get()) {
            String dataHostname = mAppPrefsValues.getData_ServerHostName();
            int dataPort = mAppPrefsValues.getData_ServerPort();

            long now = SystemClock.elapsedRealtime();
            setStatus(false, "Connecting to data server at " + dataHostname + ", port " + dataPort);
            if (DEBUG) Log.i(TAG, "Data Client Loop: connecting to: " + dataHostname + " port " + dataPort);

            try {
                try {
                    if (dataHostname.isEmpty()) throw new UnknownHostException("Empty KV Server HostName");
                    InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(dataHostname), dataPort);
                    mDataClient = new KeyValueClient(mLogger, address, mKVClientListener);
                    // TODO FIXME ++ mDataClient.setOnChangeListener(mActivity);

                    if (mDataClient.startSync()) {
                        setStatus(false, "Connected to data server at " + dataHostname + ", port " + dataPort);
                        mDataClient.requestAllKeys();
                        mDataClient.join();
                    } else {
                        long delay1sec = now + 1000 - SystemClock.elapsedRealtime();
                        if (delay1sec > 0) {
                            Thread.sleep(delay1sec);
                        }
                    }

                } catch (UnknownHostException e) {
                    if (DEBUG) Log.e(TAG, "Data Client Loop: KeyValueClient: ", e);
                    setStatus(true, "Data Server: Invalid Hostname. Please check settings.");
                }
            } catch (InterruptedException e) {
                // data.startSync or data.join got interrupted.
                // if mKeepConnected is false, the while loop will terminate otherwise we'll retry.
            }

            if (mKeepConnected.get()) {
                if (!isWifiConnected()) {
                    setStatus(true, "Connection to data server lost -- Check the Wifi connection");
                } else {
                    setStatus(true, "Connection to data server lost");
                }
                if (DEBUG) Log.i(TAG, "Data Client Loop: failed, retry after delay");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) {}
            }
        }
    }

    private boolean isWifiConnected() {
        WifiInfo info = mWifiManager.getConnectionInfo();
        return info != null && info.getNetworkId() != -1;
    }

    private void setStatus(boolean isError, String message) {
        mStatus.set(isError, message);
        mStatusPublisher.publish(mStatus);
    }

    public void stopCnx() {
        mKeepConnected.set(false);
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
