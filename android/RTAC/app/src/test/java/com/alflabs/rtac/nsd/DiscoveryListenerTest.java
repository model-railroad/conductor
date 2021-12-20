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

package com.alflabs.rtac.nsd;


import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.RtacTestConfig;
import com.alflabs.rtac.app.AppMockComponent;
import com.alflabs.rtac.app.AppPrefsValues;
import com.alflabs.rx.Schedulers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,
        sdk = RtacTestConfig.ROBOELECTRIC_SDK,
        manifest = "src/main/AndroidManifest.xml",
        application = AppMockComponent.class)
public class DiscoveryListenerTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock Context mContext;
    @Mock AppPrefsValues mAppPrefsValues;
    @Mock NsdManagerDelegate mNsdManagerDelegate;

    private DiscoveryListener mListener;

    @Before
    public void setUp() throws Exception {
        mListener = new DiscoveryListener(mContext, mAppPrefsValues) {
            @Override
            protected boolean _abortIfEmulator() {
                return false;
            }

            @Override
            protected NsdManagerDelegate _getNsdManager() {
                return mNsdManagerDelegate;
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mNsdManagerDelegate);
    }

    @Test
    public void testStart_Enabled() throws Exception {
        when(mAppPrefsValues.getSystem_EnableNsd()).thenReturn(true);
        mListener.start();
        verify(mNsdManagerDelegate).discoverServices(anyString(), anyInt(), isA(NsdManager.DiscoveryListener.class));
    }

    @Test
    public void testStart_Disabled() throws Exception {
        when(mAppPrefsValues.getSystem_EnableNsd()).thenReturn(false);
        mListener.start();
        verify(mNsdManagerDelegate, never()).discoverServices(anyString(), anyInt(), any());
    }

    @Test
    public void testStop() throws Exception {
        when(mAppPrefsValues.getSystem_EnableNsd()).thenReturn(true);
        mListener.start();
        reset(mNsdManagerDelegate);
        mListener.stop();
        verify(mNsdManagerDelegate).stopServiceDiscovery(isA(NsdManager.DiscoveryListener.class));
    }

    @Test
    public void testServiceResolved() throws Exception {
        when(mAppPrefsValues.getSystem_EnableNsd()).thenReturn(true);

        ArgumentCaptor<String> serviceTypeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<NsdManager.DiscoveryListener> discoveryListenerCaptor = ArgumentCaptor.forClass(NsdManager.DiscoveryListener.class);
        ArgumentCaptor<NsdManager.ResolveListener> resolveListenerCaptor = ArgumentCaptor.forClass(NsdManager.ResolveListener.class);

        CountDownLatch resultLatch = new CountDownLatch(1);
        AtomicReference<NsdServiceInfo> resolvedServiceInfo = new AtomicReference<>();
        mListener.getServiceResolvedStream().subscribe(
                (stream, nsdServiceInfo) -> {
                    resolvedServiceInfo.set(nsdServiceInfo);
                    resultLatch.countDown();
                },
                Schedulers.sync());
        assertThat(resolvedServiceInfo.get()).isNull();

        mListener.start();
        verify(mNsdManagerDelegate).discoverServices(serviceTypeCaptor.capture(), anyInt(), discoveryListenerCaptor.capture());
        NsdManager.DiscoveryListener discoveryListener = discoveryListenerCaptor.getValue();
        assertThat(discoveryListener).isNotNull();
        assertThat(serviceTypeCaptor.getValue()).isNotEmpty();

        NsdServiceInfo expected = mock(NsdServiceInfo.class);
        when(expected.getServiceType()).thenReturn(serviceTypeCaptor.getValue());

        discoveryListener.onServiceFound(expected);
        verify(mNsdManagerDelegate).resolveService(same(expected), resolveListenerCaptor.capture());
        NsdManager.ResolveListener resolveListener = resolveListenerCaptor.getValue();
        assertThat(resolveListener).isNotNull();

        resolveListener.onServiceResolved(expected);
        resultLatch.await(5, TimeUnit.SECONDS);
        assertThat(resolvedServiceInfo.get()).isSameAs(expected);
    }
}
