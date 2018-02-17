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
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.R;
import com.alflabs.rtac.app.MainApp;

import javax.inject.Inject;

/**
 * Service for RTAC.
 * <p/>
 * The service by itself does not handle anything.
 * Its main purpose is to keep the app alive when not in foreground in order to maintain the state and the
 * network connections alive and ready at all time. If for some reason the users exits the activity,
 * a notification is presented to bring it back.
 * There's also a wakelock on wifi to prevent it from going to sleep.
 * <p/>
 * Consequently, the service's lifecylce is tied to the "useful" lifecycle of the app (from a user point of view).
 * Although the service is started by the main activity, it does so when the app starts.
 * Similarly, the service ends when the user tells the main activity explicitely to "quit the app".
 * <p/>
 * The app is structured as having essentially one main activity that is always shown to the user.
 * This is the one that starts and binds to the service.
 * There may be accessory activities such as settings but these do not bind to the service.
 * <p/>
 * To summarize:
 * - the onCreate/onDestroy cycle matches the useful part of the app lifecycle.
 * - the onBind/onUnbind cycle matches the main activity being visible and connected to the service.
 */
public class RtacService extends android.app.Service {

    private static final String TAG = RtacService.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final int NID = 'r' << 24 + 't' << 16 + 'a' << 8 + 'c';

    private final IBinder mBinder = new LocalBinder();

    /**
     * Flag set by the first onBind() after onCreate() when the main activity binds to the service for the first time.
     * It is only unset in onDestroy() and is true even when the activity is not bound to the service.
     */
    private boolean mIsRunning;
    /**
     * Flag set when the activity starts the service.
     * It is false when the user navigates away from the main activity (settings, home, other app, etc.)
     * It is a good indication that the main activity UI is presenting the state to the user.
     */
    private boolean mIsForeground;

    @Inject NotificationManager mNotificationManager;
    @Inject DataClientMixin mDataClientMixin;
    @Inject WifiMonitorMixin mWifiMonitorMixin;
    @Inject WakeWifiLockMixin mWakeWifiLockMixin;

    @VisibleForTesting
    boolean isRunning() {
        return mIsRunning;
    }

    @VisibleForTesting
    boolean isForeground() {
        return mIsForeground;
    }

    /**
     * Must be called by the activity to start the service.
     * {@link RtacService#onStartCommand} will keep the service as sticky.
     * The caller must use the {@link LocalBinder#quitService()} later to
     * end the service.
     * <p/>
     * Note: binding to the service is not enough to keep it alive. RtacService
     * doc on the lifecycle explicitly states a bound service ends when unbound
     * which is not what we want here.
     */
    public static void startStickyService(Context ctx) {
        Intent i = new Intent(ctx, RtacService.class);
        ctx.startService(i);
    }

    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG, "onCreate");
        super.onCreate();
        Context appContext = getApplicationContext();
        MainApp.getAppComponent(appContext).inject(this);

        mWakeWifiLockMixin.onCreate(this);
        mWifiMonitorMixin.onCreate(this);
        mDataClientMixin.onCreate(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "onStartCommand[" + startId + "]: already running=" + mIsRunning);
        mIsRunning = true;
        return android.app.Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy");
        mIsRunning = false;

        mDataClientMixin.onDestroy();
        mWakeWifiLockMixin.onDestroy();
        mWifiMonitorMixin.onDestroy();

        super.onDestroy();
    }

    /** Called after onCreate once the activity binds to the service's binder object. */
    @Override
    public IBinder onBind(Intent intent) {
        if (DEBUG) Log.d(TAG, "onBind");
        mIsRunning = true;
        hideNotification();
        return mBinder;
    }

    /**
     * Called after onUnbind when the activity goes back in the foreground and
     * is reconnecting to the same service.
     */
    @Override
    public void onRebind(Intent intent) {
        if (DEBUG) Log.d(TAG, "onRebind");
        super.onRebind(intent);
        mIsRunning = true;
        hideNotification();
    }

    /**
     * Called after onBind or onRebind when the activity is no longer bound to the binder object.
     * This can mean the main activity was close due to going into settings or the home screen
     * and the service is essentially running in the background.
     */
    @Override
    public boolean onUnbind(Intent intent) {
        if (DEBUG) Log.d(TAG, "onUnbind");
        return true; // we want onRebind to be called when the activity comes back
    }

    // ----

    private void showNotification(@NonNull Activity parentActivity) {
        if (DEBUG) Log.d(TAG, "showNotification");
        Notification.Builder builder = createNotificationBuilder();
        builder.setSmallIcon(R.drawable.ic_rtac_launcher);
        builder.setContentTitle("RTAC is running");
        builder.setAutoCancel(false);

        // Create an intent with a "fake" back stack as if the activity had been launched
        // from the home screen.
        Intent i = new Intent(this, parentActivity.getClass());
        TaskStackBuilder stackBuilder = createTaskStackBuilder();
        stackBuilder.addParentStack(parentActivity.getClass());
        stackBuilder.addNextIntent(i);
        PendingIntent pending = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pending);

        Notification notif = builder.build();
        mNotificationManager.notify(NID, notif);
        startForeground(NID, notif);
        mIsForeground = true;
    }

    @VisibleForTesting
    @NonNull
    protected TaskStackBuilder createTaskStackBuilder() {
        return TaskStackBuilder.create(this);
    }

    @VisibleForTesting
    @NonNull
    protected Notification.Builder createNotificationBuilder() {
        return new Notification.Builder(this);
    }

    private void hideNotification() {
        if (DEBUG) Log.d(TAG, "hideNotification: " + (mIsForeground ? "Yes" : "No"));
        if (mIsForeground) {
            stopForeground(true /*removeNotification*/);
            mNotificationManager.cancel(NID);
            mIsForeground = false;
        }
    }

    // ----

    public class LocalBinder extends Binder {

        public boolean isRunning() {
            if (DEBUG) Log.d(TAG, "Binder.isRunning: " + mIsRunning);
            return mIsRunning;
        }

        /**
         * Called by the activity when the activity is going away.
         * This places the service as foreground with a notification to recall it.
         */
        public void startNotification(@NonNull Activity parentActivity) {
            if (DEBUG) Log.d(TAG, "Binder.startNotification");
            showNotification(parentActivity);
        }

        /**
         * Called by the activity to allow the service to close.
         */
        public void quitService() {
            if (DEBUG) Log.d(TAG, "Binder.quitService");
            mIsRunning = false;
            stopSelf();
        }
    }

}
