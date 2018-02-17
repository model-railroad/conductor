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
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class DataClientMixin extends ServiceMixin<RtacService> {
    public static final String NSD_PREFIX = "[NSD] ";
    private static final String TAG = DataClientMixin.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private final AtomicBoolean mKeepConnected = new AtomicBoolean();
    private final DataClientStatus mStatus = new DataClientStatus();
    private final List<HostPort> mHostPorts = new CopyOnWriteArrayList<>();
    /** Stream that broadcasts the client status' error message. */
    private final IStream<DataClientStatus> mStatusStream = Streams.stream();
    private final IPublisher<DataClientStatus> mStatusPublisher = Publishers.latest();
    /** Stream that broadcasts whether the client is connected. */
    private final IStream<Boolean> mConnectedStream = Streams.stream();
    private final IPublisher<Boolean> mConnectedPublisher = Publishers.latest();

    private KeyValueClient mKVClient;
    /** Stream that re-broadcasts the key changes from the KV client. */
    private final IStream<String> mKeyChangedStream = Streams.stream();
    private final IPublisher<String> mKeyChangedPublisher = Publishers.publisher();

    private final IClock mClock;
    private final ILogger mLogger;
    private final WakeWifiLockMixin mWakeWifiLockMixin;
    private final AppPrefsValues mAppPrefsValues;
    private final DiscoveryListener mNsdListener;
    private final KVClientStatsListener mKVClientListener;
    private final WifiManager mWifiManager;

    @Inject
    public DataClientMixin(
            IClock clock,
            ILogger logger,
            WakeWifiLockMixin wakeWifiLockMixin,
            AppPrefsValues appPrefsValues,
            DiscoveryListener nsdListener,
            KVClientStatsListener kvClientListener,
            WifiManager wifiManager) {
        mClock = clock;
        mLogger = logger;
        mWakeWifiLockMixin = wakeWifiLockMixin;
        mAppPrefsValues = appPrefsValues;
        mNsdListener = nsdListener;
        mKVClientListener = kvClientListener;
        mWifiManager = wifiManager;

        mStatusStream.publishWith(mStatusPublisher);
        mConnectedStream.publishWith(mConnectedPublisher);
        mKeyChangedStream.publishWith(mKeyChangedPublisher);
    }

    /** Stream that broadcasts the client status' error message. */
    @NonNull
    public IStream<DataClientStatus> getStatusStream() {
        return mStatusStream;
    }

    /** Stream that broadcasts whether the client is connected. */
    @NonNull
    public IStream<Boolean> getConnectedStream() {
        return mConnectedStream;
    }

    /** Stream that re-broadcasts the key changes from the KV client. */
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

        // Add existing data server host/port if any in the preferences
        String dataHostname = mAppPrefsValues.getData_ServerHostName();
        int dataPort = mAppPrefsValues.getData_ServerPort();
        if (!dataHostname.isEmpty() && !dataHostname.equals("localhost")) {
            HostPort hostPort = new HostPort(dataHostname, dataPort, false);
            if (!mHostPorts.contains(hostPort)) {
                mHostPorts.add(0, hostPort);
            }
        }

        boolean useNsd = mNsdListener.start();
        if (DEBUG) Log.d(TAG, "Data Client Loop: Using NSD = " + useNsd);

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
            HostPort hostPort = new HostPort(host.getHostAddress(), port, true);
            if (!mHostPorts.contains(hostPort)) {
                if (DEBUG) Log.d(TAG, "Data: Add NSD " + hostPort);
                mHostPorts.add(hostPort);
            }
        }
    }

    private void connectLoop() {
        while (mKeepConnected.get()) {
            mClock.sleep(1000);
            if (mHostPorts.isEmpty()) {
                setStatus(true, "Data Server: No host discovered yet. Please check settings.");
                mConnectedPublisher.publish(false);
                continue;
            }

            for (HostPort hostPort : mHostPorts) {
                if (connectToHost(hostPort)) {
                    // The connection was successful to this port and has now terminated.
                    if (DEBUG) Log.i(TAG, "Data Client Loop: End of connection with " + hostPort);

                    // Move the hostport to the top of the list so that we retry it first the next time.
                    mHostPorts.remove(hostPort);
                    mHostPorts.add(0, hostPort);

                    // Since obviously we got disconnected from a good host, try to figure why and
                    // most important, clearly state we are not connected anymore.
                    mConnectedPublisher.publish(false);
                    if (mKeepConnected.get()) {
                        if (!isWifiConnected()) {
                            setStatus(true, "Connection to data server lost -- Check the Wifi connection");
                        } else {
                            setStatus(true, "Connection to data server lost");
                        }
                    }
                    // Done iterating through hosts.
                    break;
                } else {
                    setStatus(true, "Failed to connect to data server at " + hostPort.getHostAddress() + ", port " + hostPort.getPort());
                    mConnectedPublisher.publish(false);
                    mClock.sleep(1000);
                }
            }
        }
    }

    /**
     * Tries to connect to a host/port.
     *
     * @param hostPort The non-null host port.
     * @return True if we managed to connect and the caller should stop iterating on hosts..
     *      False to signal to the caller to move on the next host.
     */
    private boolean connectToHost(@NonNull HostPort hostPort) {
        boolean success = false;
        if (DEBUG) Log.i(TAG, "Data Client Loop: Try connecting to " + hostPort);
        String dataHostname = hostPort.getHostAddress();
        int dataPort = hostPort.getPort();
        setStatus(false, "Connecting to data server at " + dataHostname + ", port " + dataPort + (hostPort.isNsd() ? " [NSD]" : ""));

        try {
            try {
                if (dataHostname.isEmpty()) throw new UnknownHostException("Empty KV Server HostName");
                InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(dataHostname), dataPort);

                if (address.getAddress() instanceof Inet6Address) {
                    // Do we accept IPv6?
                    if (!mAppPrefsValues.getSystem_EnableNsdIpv6()) {
                        if (DEBUG) Log.i(TAG, "Data Client Loop: Reject IPv6 " + hostPort);
                        return false;
                    }
                }

                // Create a KV client. We are not connected yet.
                mKVClient = new KeyValueClient(mClock, mLogger, address, mKVClientListener);
                mKVClient.getChangedStream().subscribe((stream, key) -> mKeyChangedPublisher.publish(key));

                // Start connecting synchronously. Returns true if the connection worked (and has been forked)
                if (mKVClient.startSync()) {
                    mWakeWifiLockMixin.lock(); // released in finally block

                    success = true;
                    setStatus(false, "Connected to data server at " + dataHostname + ", port " + dataPort);
                    mConnectedPublisher.publish(true);

                    // Update the settings
                    mAppPrefsValues.setData_ServerHostName(hostPort.getHostAddressWithNsdPrefix());
                    mAppPrefsValues.setData_ServerPort(hostPort.getPort());

                    mKVClient.requestAllKeys();
                    // Block till we loose the KV client connection
                    mKVClient.join();
                }

            } catch (UnknownHostException e) {
                if (DEBUG) Log.e(TAG, "Data Client Loop: KeyValueClient: ", e);
                setStatus(true, "Data Server: Invalid Hostname. Please check settings.");

            } finally {
                mWakeWifiLockMixin.release();
            }
        } catch (InterruptedException e) {
            // data.startSync or data.join got interrupted.
            // if mKeepConnected is false, the caller loop will terminate otherwise we'll retry.
        }

        return success;
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

    private static class HostPort {
        private final String mHostAddress;
        private final int mPort;
        private final boolean mIsNsd;

        public HostPort(@NonNull String hostAddress, int port, boolean isNsd) {
            if (hostAddress.startsWith(NSD_PREFIX)) {
                hostAddress = hostAddress.substring(NSD_PREFIX.length());
                isNsd = true;
            }

            mHostAddress = hostAddress;
            mPort = port;
            mIsNsd = isNsd;
        }

        @NonNull
        public String getHostAddress() {
            return mHostAddress;
        }

        @NonNull
        public String getHostAddressWithNsdPrefix() {
            return (mIsNsd ? NSD_PREFIX : "") + mHostAddress;
        }

        public int getPort() {
            return mPort;
        }

        public boolean isNsd() {
            return mIsNsd;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            HostPort hostPort = (HostPort) o;

            if (mPort != hostPort.mPort) return false;
            return mHostAddress.equals(hostPort.mHostAddress);
        }

        @Override
        public int hashCode() {
            int result = mHostAddress.hashCode();
            result = 31 * result + mPort;
            return result;
        }

        @Override
        public String toString() {
            return "HostPort {"+ mHostAddress + ':' + mPort + (mIsNsd ? " [NSD]" : "") + '}';
        }
    }
}
