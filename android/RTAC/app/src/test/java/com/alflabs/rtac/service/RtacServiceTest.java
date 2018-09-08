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

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.RtacTestConfig;
import com.alflabs.rtac.app.AppMockComponent;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowService;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,
        sdk = RtacTestConfig.ROBOELECTRIC_SDK,
        manifest = "src/main/AndroidManifest.xml",
        application = AppMockComponent.class)
public class RtacServiceTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    // Note: Do not attempt to mock NotificationCompat.Builder.
    // This generates an infinite loop with repeated exceptions
    // java.lang.ClassNotFoundException: android/support/v4/app/NotificationCompatBase$Action
    // Using a @Mock Notification.Builder works fine instead.
    @Mock Notification.Builder mNotificationBuilder;
    @Mock Notification mNotification;
    @Mock TaskStackBuilder mTaskStackBuilder;
    @Mock PackageManager mPackageManager;

    private NotificationManager mNotifManager;
    private RtacService mRtacService;

    @Before
    public void setUp() throws Exception {
        AppMockComponent appMockComponent = (AppMockComponent) RuntimeEnvironment.application;
        mNotifManager = appMockComponent.getAppContextModule().providesNotificationManager();

        mRtacService = new RtacService() {
            @Override
            public String getPackageName() {
                return RuntimeEnvironment.application.getPackageName();
            }

            @Override
            public PackageManager getPackageManager() {
                return mPackageManager;
            }

            @Override
            public Context getApplicationContext() {
                return RuntimeEnvironment.application;
            }

            @Override
            protected Notification.Builder createNotificationBuilder() {
                when(mNotificationBuilder.build()).thenReturn(mNotification);
                return mNotificationBuilder;
            }

            @NonNull
            @Override
            protected TaskStackBuilder createTaskStackBuilder() {
                return mTaskStackBuilder;
            }
        };
        mRtacService.onCreate();
        assertThat(mRtacService.isRunning()).isFalse();
        assertThat(mRtacService.isForeground()).isFalse();
    }

    @Test
    public void testOnStartCommand() throws Exception {
        mRtacService.onStartCommand(new Intent(), 0, 1);
        assertThat(mRtacService.isRunning()).isTrue();
    }

    @Test
    public void testOnDestroy() throws Exception {
        assertThat(mRtacService.isRunning()).isFalse();
    }

    @Test
    public void testActivityBinds() throws Exception {
        mRtacService.onStartCommand(new Intent(), 0, 1);
        RtacService.LocalBinder binder = (RtacService.LocalBinder) mRtacService.onBind(new Intent());
        assertThat(mRtacService.isForeground()).isFalse();

        assertThat(binder).isNotNull();
        assertThat(binder.isRunning()).isTrue();
        assertThat(mRtacService.isForeground()).isFalse();
    }

    @Test
    public void testActivityIsDismissedThenResumed() throws Exception {
        Activity activity = Robolectric.buildActivity(Activity.class).setup().get();

        mRtacService.onStartCommand(new Intent(), 0, 1);
        RtacService.LocalBinder binder = (RtacService.LocalBinder) mRtacService.onBind(new Intent());
        assertThat(mRtacService.isForeground()).isFalse();

        binder.startNotification(activity);

        assertThat(mRtacService.isForeground()).isTrue();

        ArgumentCaptor<Integer> id = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Notification> notif = ArgumentCaptor.forClass(Notification.class);
        verify(mNotifManager).notify(id.capture(), notif.capture());

        ShadowService shadowService = Shadows.shadowOf(mRtacService);
        assertThat(shadowService.getLastForegroundNotificationId()).isEqualTo(id.getValue());
        assertThat(shadowService.getLastForegroundNotification()).isEqualTo(notif.getValue());

        mRtacService.onRebind(new Intent());
        assertThat(mRtacService.isForeground()).isFalse();
        verify(mNotifManager).cancel(id.getValue());
        assertThat(shadowService.isForegroundStopped()).isTrue();
    }
}
