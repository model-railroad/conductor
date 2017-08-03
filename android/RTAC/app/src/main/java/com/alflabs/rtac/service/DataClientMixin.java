package com.alflabs.rtac.service;

import android.net.nsd.NsdServiceInfo;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;
import com.alflabs.kv.KeyValueClient;
import com.alflabs.sub.Emitter;
import com.alflabs.utils.ServiceMixin;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class DataClientMixin extends ServiceMixin<RtacService> {

    private TextView mDataStatusText;
    private final AtomicBoolean mTryToConnect = new AtomicBoolean();

    private final Emitter<DataClientStatus> mStatusEmitter = new Emitter<>();

    @Inject
    public DataClientMixin() {}

    public Emitter<DataClientStatus> getStatusEmitter() {
        return mStatusEmitter;
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
// FIXME import for JED to be rewritten
//        if (AppGlobals.getDataServer() == null) {
//            mDataStatusText.setBackgroundColor(Colors.STATUS_BG_ERROR);
//            mDataStatusText.setText("Data Client: Not Started");
//        }
//
//        mTryToConnect.set(true);
//        Thread t = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                KeyValueClient dataClient = AppGlobals.getDataClient();
//                if (dataClient != null) {
//                    dataClient.stopSync();
//                    AppGlobals.setDataClient(null);
//                }
//
//                DiscoveryListener nsdListener = new DiscoveryListener(mActivity) {
//                    @Override
//                    public void onServiceResolved(@NonNull NsdServiceInfo serviceInfo) {
//                        InetAddress host = serviceInfo.getHost();
//                        int port = serviceInfo.getPort();
//                        if (DEBUG) Log.e(TAG, "Data Client Loop: onServiceResolved " + host.getHostAddress() + " port " + port);
//                        if (host != null && port > 0) {
//                            AppPrefsValues prefs = AppGlobals.getAppPrefs();
//                            prefs.setDataServerHostName(host.getHostAddress());
//                            prefs.setDataServerPort(port);
//                        }
//                    }
//                };
//
//                boolean useNsd = nsdListener.start();
//                if (useNsd) {
//                    AppGlobals.getAppPrefs().setDataServerHostName("");
//                }
//
//                while (mTryToConnect.get() && AppGlobals.getDataClient() == null) {
//                    String dataHostname = AppGlobals.getAppPrefs().getDataServerHostName();
//                    int dataPort = AppGlobals.getAppPrefs().getDataServerPort();
//
//                    if (DEBUG) Log.e(TAG, "Data Client Loop: connecting to: " + dataHostname + " port " + dataPort);
//
//                    try {
//                        try {
//                            if (dataHostname.isEmpty()) throw new UnknownHostException("Empty KV Server HostName");
//                            InetSocketAddress address = new InetSocketAddress(InetAddress.getByName(dataHostname), dataPort);
//                            KeyValueClient data = new KeyValueClient(address);
//                            AppGlobals.setDataClient(data);
//                            data.setOnChangeListener(mActivity);
//
//                            if (data.startSync()) {
//                                mTryToConnect.set(false);
//                                data.requestAllKeys();
//                            }
//
//                        } catch (UnknownHostException e) {
//                            if (DEBUG) Log.e(TAG, "Data Client Loop: KeyValueClient: ", e);
//                            mActivity.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    mDataStatusText.setText("Data Server: Invalid Hostname. Please check settings.");
//                                }
//                            });
//                        }
//
//                        if (mTryToConnect.get()) {
//                            if (DEBUG) Log.e(TAG, "Data Client Loop: failed, retry in 2 seconds");
//                            Thread.sleep(2000);
//                        }
//                    } catch (InterruptedException e) {
//                        // data.startSync or thread.sleep got interrupted.
//                        // if mTryToConnect is false, the while loop will terminate
//                        // otherwise we'll retry.
//                    }
//                }
//                if (DEBUG) Log.e(TAG, "Data Client Loop: finished");
//
//                if (useNsd) {
//                    nsdListener.stop();
//                }
//            }
//        }, "StarDataClient-Thread");
//        t.start();
    }

    public void stopCnx() {
// FIXME import for JED to be rewritten
//        mTryToConnect.set(false);
//        KeyValueClient dataClient = AppGlobals.getDataClient();
//        if (dataClient != null) {
//            dataClient.stopAsync();
//            AppGlobals.setDataClient(null);
//            mDataStatusText.setText("Data Server: Disconnected.");
//        }
    }

    public static class DataClientStatus {
        private boolean mIsError;
        String mText;

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
