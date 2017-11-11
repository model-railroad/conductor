/*
 * Project: RTAC
 * Copyright (C) 2017 alf.labs gmail com,
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

package com.alflabs.rtac.service;

import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.kv.KeyValueClient;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.app.AppPrefsValues;
import com.alflabs.rtac.nsd.DiscoveryListener;
import com.alflabs.rx.IPublisher;
import com.alflabs.rx.IStream;
import com.alflabs.rx.ISubscriber;
import com.alflabs.rx.Publishers;
import com.alflabs.rx.Streams;
import com.alflabs.utils.IClock;
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
    public static final String NSD_PREFIX = "[NSD] ";
    private static final String TAG = DataClientMixin.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private final AtomicBoolean mKeepConnected = new AtomicBoolean();
    private final DataClientStatus mStatus = new DataClientStatus();
    private final IStream<DataClientStatus> mStatusStream = Streams.stream();
    private final IPublisher<DataClientStatus> mStatusPublisher = Publishers.latest();

    private KeyValueClient mKVClient;
    private final IStream<String> mKeyChangedStream = Streams.stream();
    private final IPublisher<String> mKeyChangedPublisher = Publishers.publisher();

    private final IClock mClock;
    private final ILogger mLogger;
    private final AppPrefsValues mAppPrefsValues;
    private final DiscoveryListener mNsdListener;
    private final KVClientStatsListener mKVClientListener;
    private final WifiManager mWifiManager;

    @Inject
    public DataClientMixin(
            IClock clock,
            ILogger logger,
            AppPrefsValues appPrefsValues,
            DiscoveryListener nsdListener,
            KVClientStatsListener kvClientListener,
            WifiManager wifiManager) {
        mClock = clock;
        mLogger = logger;
        mAppPrefsValues = appPrefsValues;
        mNsdListener = nsdListener;
        mKVClientListener = kvClientListener;
        mWifiManager = wifiManager;

        mStatusStream.publishWith(mStatusPublisher);
        mKeyChangedStream.publishWith(mKeyChangedPublisher);
    }

    @NonNull
    public IStream<DataClientStatus> getStatusStream() {
        return mStatusStream;
    }

    @NonNull
    public IStream<String> getKeyChangedStream() {
        return mKeyChangedStream;
    }

    @Null
    public KeyValueClient getKeyValueClient() {
        return mKVClient;
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
        if (mKVClient == null) {
            setStatus(true, "Data Client: Not Started");
        }

        mKeepConnected.set(true);
        Thread t = new Thread(this::startCnxOnThread, "DataClient-Thread");
        t.start();
    }

    private void startCnxOnThread() {
        if (mKVClient != null) {
            mKVClient.stopSync();
            mKVClient = null;
        }

        ISubscriber<NsdServiceInfo> nsdSubscriber = (stream, serviceInfo) -> onNsdServiceFound(serviceInfo);

        mNsdListener.getServiceResolvedStream().subscribe(nsdSubscriber);

        boolean useNsd = mNsdListener.start();
        if (useNsd) {
            // Only remove the current hostname if it didn't come from NSD
            String dataHostname = mAppPrefsValues.getData_ServerHostName();
            if (!dataHostname.startsWith(NSD_PREFIX)) {
                mAppPrefsValues.setData_ServerHostName("");
            }
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
            mAppPrefsValues.setData_ServerHostName(NSD_PREFIX + host.getHostAddress());
            mAppPrefsValues.setData_ServerPort(port);
        }
    }

    private void connectLoop() {
        while (mKeepConnected.get()) {
            String dataHostname = mAppPrefsValues.getData_ServerHostName();
            int dataPort = mAppPrefsValues.getData_ServerPort();

            if (dataHostname.startsWith(NSD_PREFIX)) {
                dataHostname = dataHostname.substring(NSD_PREFIX.length());
            }

            long now = mClock.elapsedRealtime();
            setStatus(false, "Connecting to data server at " + dataHostname + ", port " + dataPort);
            if (DEBUG) Log.i(TAG, "Data Client Loop: connecting to: " + dataHostname + " port " + dataPort);

            try {
                try {
                    if (dataHostname.isEmpty()) throw new UnknownHostException("Empty KV Server HostName");
                    InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(dataHostname), dataPort);
                    mKVClient = new KeyValueClient(mClock, mLogger, address, mKVClientListener);
                    mKVClient.getChangedStream().subscribe((stream, key) -> mKeyChangedPublisher.publish(key));

                    if (mKVClient.startSync()) {
                        setStatus(false, "Connected to data server at " + dataHostname + ", port " + dataPort);
                        mKVClient.requestAllKeys();
                        mKVClient.join();
                    } else {
                        long delay1sec = now + 1000 - mClock.elapsedRealtime();
                        if (delay1sec > 0) {
                            mClock.sleep(delay1sec);
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
                    mClock.sleep(1000);
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
        if (mKVClient != null) {
            mKVClient.stopAsync();
            mKVClient = null;
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
