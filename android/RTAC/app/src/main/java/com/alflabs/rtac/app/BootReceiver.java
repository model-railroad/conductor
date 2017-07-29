package com.alflabs.rtac.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.activity.MainActivity;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = BootReceiver.class.getSimpleName();
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /** Intent to open RTAC directly. Must match BootReceiver definition in AndroidManifest.xml. */
    public static final String ACTION_OPEN_RTAC = "com.alflabs.rtac.intent.action.OPEN_RTAC";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG) Log.d(TAG, "onReceive: intent=" + intent);
        if (intent == null) return;

        String action = intent.getAction();
        boolean openRTAC = ACTION_OPEN_RTAC.equals(action);

        if (!openRTAC) {
            if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
                AppPrefsValues prefsValues = MainApp.getAppComponent(context).getAppPrefsValues();
                AppPrefsValues.BootAction bootAction = prefsValues.getSystem_BootAction();
                if (DEBUG) Log.d(TAG, "onReceive: boot action=" + bootAction);
                if (bootAction == AppPrefsValues.BootAction.START_RTAC) {
                    Intent i = new Intent(context, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                }
            }
        }

        if (openRTAC) {
            Intent i = new Intent(context, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.fillIn(intent, 0);
            context.startActivity(i);
        }
    }
}
