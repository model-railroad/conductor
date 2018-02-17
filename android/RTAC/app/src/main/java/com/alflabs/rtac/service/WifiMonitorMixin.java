package com.alflabs.rtac.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.app.AppPrefsValues;
import com.alflabs.utils.ServiceMixin;

import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class WifiMonitorMixin extends ServiceMixin<RtacService> {
    private static final String TAG = WifiMonitorMixin.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private final WifiManager mWifiManager;
    private final AppPrefsValues mAppPrefsValues;
    private BroadcastReceiver mScreenOnReceiver;

    @Inject
    public WifiMonitorMixin(
            WifiManager wifiManager,
            AppPrefsValues appPrefsValues) {
        mWifiManager = wifiManager;
        mAppPrefsValues = appPrefsValues;
    }

    @Override
    public void onCreate(RtacService service) {
        super.onCreate(service);
        onScreenOn();
        registerScreenOnReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterScreenOnReceiver();
    }

    private void registerScreenOnReceiver() {
        if (mScreenOnReceiver != null) return;

        mScreenOnReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onScreenOn();
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);

        Context context = getService().getApplicationContext();
        context.registerReceiver(mScreenOnReceiver, filter);
    }

    private void unregisterScreenOnReceiver() {
        if (mScreenOnReceiver != null) {
            Context context = getService().getApplicationContext();
            context.unregisterReceiver(mScreenOnReceiver);
            mScreenOnReceiver = null;
        }
    }

    private void onScreenOn() {
        String ssid = mAppPrefsValues.getSystem_WifiSsid();
        if (DEBUG) Log.d(TAG, "onScreenOn, checking for SSID: " + ssid);

        if (ssid == null || ssid.trim().isEmpty()) {
            return;
        }
        ssid = ssid.trim();
        String quoted_ssid = '"' + ssid + '"';

        if (!mWifiManager.isWifiEnabled()) {
            if (DEBUG) Log.d(TAG, "Enable Wifi");
            mWifiManager.setWifiEnabled(true);
        }

        // Check current state.
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo != null &&
                (ssid.equals(wifiInfo.getSSID()) || quoted_ssid.equals(wifiInfo.getSSID()))) {
            // Already connected.
            if (DEBUG) Log.d(TAG, "Current Wifi: " + wifiInfo.toString());
            return;
        }

        for (WifiConfiguration configuration : mWifiManager.getConfiguredNetworks()) {
            if (ssid.equals(configuration.SSID) || quoted_ssid.equals(configuration.SSID)) {
                if (DEBUG) Log.d(TAG, "Enable Wifi: " + configuration.SSID);
                mWifiManager.enableNetwork(configuration.networkId, true /*attempConnect*/ );
                break;
            }
        }
    }
}

