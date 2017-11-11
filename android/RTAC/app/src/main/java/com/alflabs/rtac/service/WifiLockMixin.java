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

    private WifiManager mWifiManager;

    @Inject
    public WifiLockMixin(WifiManager wifiManager) {
        mWifiManager = wifiManager;
    }

    @Override
    public void onCreate(RtacService service) {
        super.onCreate(service);

        // TODO have a pref to toggle between FULL and FULL_HIGH_PERF (with high being default)
        try {
            mWifiLock = mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "RTAC Service");
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
