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

import android.annotation.SuppressLint;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.util.Log;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.utils.ServiceMixin;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Hold both a wake lock and a wifi lock when desired.
 * <p/>
 * This is currently used to acquire both locks once a connection is open
 * and release it when the connection is dropped.
 */
@Singleton
public class WakeWifiLockMixin extends ServiceMixin<RtacService> {
    private static final String TAG = WakeWifiLockMixin.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    private final PowerManager mPowerManager;
    private final WifiManager mWifiManager;

    private WifiManager.WifiLock mWifiLock;
    private PowerManager.WakeLock mWakeLock;

    @Inject
    public WakeWifiLockMixin(PowerManager powerManager, WifiManager wifiManager) {
        mPowerManager = powerManager;
        mWifiManager = wifiManager;
    }

    @Override
    public void onCreate(RtacService service) {
        super.onCreate(service);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }

    public void lock() {
        lockWake();
        lockWifi();
    }

    public void release() {
        releaseWake();
        releaseWifi();
    }

    @SuppressLint("WakelockTimeout")
    private void lockWake() {
        if (mWakeLock != null) return;
        try {
            mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "RTAC:Service");
            mWakeLock.acquire();
            if (DEBUG) Log.d(TAG, "Wake Lock Acquire");
        } catch (Throwable e) {
            Log.e(TAG, "Wake Lock Acquire failed", e);
        }
    }

    private void releaseWake() {
        if (mWakeLock == null) return;
        try {
            if (DEBUG) Log.d(TAG, "Wake Lock Release");
            mWakeLock.release();
        } catch (Throwable e) {
            Log.e(TAG, "Wake Lock Release failed", e);
        }
        mWakeLock = null;
    }

    private void lockWifi() {
        if (mWifiLock != null) return;
        try {
            mWifiLock = mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "RTAC Service");
            mWifiLock.acquire();
            if (DEBUG) Log.d(TAG, "Wifi Lock Acquire");
        } catch (Throwable e) {
            Log.e(TAG, "Wifi Lock Acquire failed", e);
        }
    }

    private void releaseWifi() {
        if (mWifiLock == null) return;
        try {
            if (DEBUG) Log.d(TAG, "Wifi Lock Release");
            mWifiLock.release();
        } catch (Throwable e) {
            Log.e(TAG, "Wifi Lock Release failed", e);
        }
        mWifiLock = null;
    }
}
