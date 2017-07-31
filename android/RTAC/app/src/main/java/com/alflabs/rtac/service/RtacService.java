package com.alflabs.rtac.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.R;

/**
 * Service for RTAC.
 * <p/>
 * The service by itself does not handle anything.
 * Its main purpose is to keep the app alive when not in foreground,
 * with a notification to bring it back. It also adds a wakelock on
 * wifi to prevent it from going to sleep.
 */
public class RtacService extends android.app.Service {

    private static final String TAG = RtacService.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;
    private static final int NID = 'r' << 24 + 't' << 16 + 'a' << 8 + 'c';

    private final IBinder mBinder = new LocalBinder();
    private boolean mIsRunning;
    private NotificationManager mNotifMan;
    private WifiManager.WifiLock mWifiLock;
    private boolean mIsForeground;

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
        mNotifMan = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);

        WifiManager wifiMan = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        // TODO have a pref to toggle between FULL and FULL_HIGH_PERF (with high being default)
        try {
            mWifiLock = wifiMan.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "Cab Throttle Service");
            mWifiLock.acquire();
            if (DEBUG) Log.d(TAG, "Wifi Lock Acquire");
        } catch (Throwable e) {
            Log.e(TAG, "Wifi Lock Acquire failed", e);
        }

        HandlerThread thread = new HandlerThread(TAG + "_handler");
        thread.start();
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
        try {
            if (DEBUG) Log.d(TAG, "Wifi Lock Release");
            mWifiLock.release();
        } catch (Throwable e) {
            Log.e(TAG, "Wifi Lock Release failed", e);
        }
        mWifiLock = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (DEBUG) Log.d(TAG, "onBind");
        mIsRunning = true;
        hideNotification();
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        if (DEBUG) Log.d(TAG, "onRebind");
        super.onRebind(intent);
        mIsRunning = true;
        hideNotification();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (DEBUG) Log.d(TAG, "onUnbind");
        return true; // we want onRebind to be called when the activity comes back
    }

    // ----

    private void showNotification(@NonNull Activity parentActivity) {
        if (DEBUG) Log.d(TAG, "showNotification");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_rtac_launcher);
        builder.setContentTitle("RTAC is running");
        builder.setAutoCancel(false);

        // Create an intent with a "fake" back stack as if the activity had been launched
        // from the home screen.
        Intent i = new Intent(this, parentActivity.getClass());
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(parentActivity.getClass());
        stackBuilder.addNextIntent(i);
        PendingIntent pending = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pending);

        Notification not = builder.build();
        mNotifMan.notify(NID, not);
        startForeground(NID, not);
        mIsForeground = true;
    }

    private void hideNotification() {
        if (DEBUG) Log.d(TAG, "hideNotification: " + (mIsForeground ? "Yes" : "No"));
        if (mIsForeground) {
            stopForeground(true /*removeNotification*/);
            mNotifMan.cancel(NID);
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
