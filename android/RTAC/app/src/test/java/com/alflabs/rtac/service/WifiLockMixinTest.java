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
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.app.AppMockComponent;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,
        sdk = 19,
        manifest = "src/main/AndroidManifest.xml",
        application = AppMockComponent.class)
public class WifiLockMixinTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock RtacService mRtacService;
    @Mock WifiManager.WifiLock mWifiLock;
    private WifiLockMixin mMixin;
    private WifiManager mWifiManager;

    @Before
    public void setUp() throws Exception {
        AppMockComponent appMockComponent = (AppMockComponent) RuntimeEnvironment.application;
        mWifiManager = appMockComponent.getAppContextModule().providesWifiManager();
        assertThat(mWifiManager).isNotNull();

        when(mWifiManager.createWifiLock(anyInt(), anyString())).thenReturn(mWifiLock);

        mMixin = appMockComponent.getAppComponent().getWifiLockMixin();
        assertThat(mMixin).isNotNull();
    }

    @Test
    public void testOnCreate() throws Exception {
        mMixin.onCreate(mRtacService);
        verify(mWifiManager).createWifiLock(eq(WifiManager.WIFI_MODE_FULL_HIGH_PERF), anyString());
        verify(mWifiLock).acquire();
    }

    @Test
    public void testOnDestroy() throws Exception {
        mMixin.onCreate(mRtacService);
        reset(mWifiLock);
        mMixin.onDestroy();
        verify(mWifiLock).release();
    }
}
