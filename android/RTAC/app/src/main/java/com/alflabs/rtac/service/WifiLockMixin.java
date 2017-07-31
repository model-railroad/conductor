package com.alflabs.rtac.service;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.utils.ServiceMixin;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WifiLockMixin extends ServiceMixin<RtacService> {
    private static final String TAG = WifiLockMixin.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private WifiManager.WifiLock mWifiLock;

    @Inject
    public WifiLockMixin() {}

    @Override
    public void onCreate(RtacService service) {
        super.onCreate(service);
        Context appContext = service.getApplicationContext();
        WifiManager wifiMan = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);

        // TODO have a pref to toggle between FULL and FULL_HIGH_PERF (with high being default)
        try {
            mWifiLock = wifiMan.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "RTAC Service");
            mWifiLock.acquire();
            if (DEBUG) Log.d(TAG, "Wifi Lock Acquire");
        } catch (Throwable e) {
            Log.e(TAG, "Wifi Lock Acquire failed", e);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (DEBUG) Log.d(TAG, "Wifi Lock Release");
            mWifiLock.release();
        } catch (Throwable e) {
            Log.e(TAG, "Wifi Lock Release failed", e);
        }
        mWifiLock = null;
    }
}
