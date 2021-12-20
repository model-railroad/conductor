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

package com.alflabs.rtac.app;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import com.alflabs.annotations.NonNull;
import com.alflabs.dagger.AppQualifier;
import com.alflabs.utils.AndroidLogger;
import com.alflabs.utils.ILogger;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
@SuppressWarnings("WeakerAccess")
public class AppContextModule {

    @NonNull
    private final Context mContext;

    public AppContextModule(@NonNull Context context) {
        mContext = context;
    }

    /**
     * Provides an Android context, specifically this one from the app component.
     * Users request it by using the @AppQualifier to distinguish it from the one provided by the activity.
     */
    @NonNull
    @Provides
    @AppQualifier
    public Context providesContext() {
        return mContext;
    }

    /**
     * Provides a singleton instance of the android logger. This method doesn't do any logic
     * to make sure it's a singleton. However in the DaggerIAppComponent, the result is wrapped
     * in a DoubleCheck that will cache and return a singleton value. Because it's a @Singleton
     * it is also app-wide and shared with all sub-components.
     */
    @NonNull
    @Provides
    @Singleton
    public ILogger providesLogger() {
        return new AndroidLogger();
    }

    @NonNull
    @Provides
    @Singleton
    public AlarmManager providesAlarmManager() {
        return (AlarmManager) mContext.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
    }

    @NonNull
    @Provides
    @Singleton
    public WifiManager providesWifiManager() {
        return (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    @NonNull
    @Provides
    @Singleton
    public PowerManager providesPowerManager() {
        return (PowerManager) mContext.getApplicationContext().getSystemService(Context.POWER_SERVICE);
    }

    @NonNull
    @Provides
    @Singleton
    public NotificationManager providesNotificationManager() {
        return (NotificationManager) mContext.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }
}
