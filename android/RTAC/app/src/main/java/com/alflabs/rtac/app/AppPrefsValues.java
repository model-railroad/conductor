package com.alflabs.rtac.app;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.alflabs.dagger.AppQualifier;
import com.alflabs.prefs.BasePrefsValues;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Locale;


@Singleton
public class AppPrefsValues extends BasePrefsValues {

    public enum BootAction {
        NO_ACTION,
        START_RTAC
    }

    public static final String PREF_SYSTEM__HIDE_NAVIGATION = "pref_system__hide_navigation";
    public static final String PREF_SYSTEM__BOOT_ACTION = "pref_system__boot_action";
    public static final String PREF_SYSTEM__ENABLE_NSD = "pref_system__enable_nsd";

    public static final String PREF_DATA__SERVER_HOSTNAME = "pref_data__server_hostname";
    public static final String PREF_DATA__SERVER_PORT     = "pref_data__server_port";

// Imported from JED... remove or use later.
//    public static final String PREF_SYSTEM__COLOR_SET = "pref_system__color_set";
//
//    public static final String PREF_JMRI__SERVER_HOSTNAME = "pref_jmri__server_hostname";
//    public static final String PREF_JMRI__SERVER_PORT     = "pref_jmri__server_port";
//    public static final String PREF_JMRI__THROTTLE_NAME   = "pref_jmri__throttle_name";
//    public static final String PREF_JMRI__FORCE_MULTI_THROTTLE = "pref_jmri__force_multi_throttle";
//    public static final String PREF_JMRI__ONE_SOCKET_PER_THROTTLE = "pref_jmri__one_socket_per_throttle";

    @Inject
    public AppPrefsValues(@AppQualifier Context context) {
        super(context);
    }

// Imported from JED... remove or use later.
//    public void checkDefaultsAsync() {
//        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                if (mPrefs.getString(PREF_JMRI__THROTTLE_NAME, null) == null) {
//                    String name = NetworkUtils.getWifiAndroidDeviceName();
//                    if (name != null) {
//                        setJmriThrottleName(name);
//                    }
//                }
//                return null;
//            }
//        };
//
//        task.execute();
//    }

    /** Retrieve string for key or null. */
    @Nullable
    public String getString(@NonNull String key) {
        return mPrefs.getString(key, null);
    }

    /** Sets or removes (null) a key string. */
    public void setString(@NonNull String key, @Nullable String value) {
        synchronized (editLock()) {
            endEdit(startEdit().putString(key, value));
        }
    }

    public boolean getSystem_HideNavigation() {
        return mPrefs.getBoolean(PREF_SYSTEM__HIDE_NAVIGATION, true);
    }

    public boolean getSystem_EnableNsd() {
        return mPrefs.getBoolean(PREF_SYSTEM__ENABLE_NSD, true);
    }

    @NonNull
    public BootAction getSystem_BootAction() {
        String s = mPrefs.getString(PREF_SYSTEM__BOOT_ACTION, BootAction.NO_ACTION.toString());
        BootAction action = null;
        try {
            action = BootAction.valueOf(s.toUpperCase(Locale.US));
        } catch (Exception ignore) {}
        return action == null ? BootAction.NO_ACTION : action;
    }

    public void setSystem_BootAction(BootAction action) {
        setString(PREF_SYSTEM__BOOT_ACTION, action.toString().toLowerCase(Locale.US));
    }

// Imported from JED... remove or use later.
//    public int getSystem_ColorSet() {
//        String s = mPrefs.getString(PREF_SYSTEM__COLOR_SET, "pastel");
//        return "saturated".equals(s) ? 2 : 1;
//    }
//
//    @NonNull
//    public String getJmriThrottleName() {
//        return mPrefs.getString(PREF_JMRI__THROTTLE_NAME, "");
//    }
//
//    public void setJmriThrottleName(@NonNull String name) {
//        synchronized (editLock()) {
//            endEdit(startEdit().putString(PREF_JMRI__THROTTLE_NAME, name));
//        }
//    }

// Imported from JED... remove or use later.
//    @NonNull
//    public String getJmriServerHostName() {
//        return mPrefs.getString(PREF_JMRI__SERVER_HOSTNAME, "");
//    }
//
//    public int getJmriServerPort() {
//        try {
//            return Integer.parseInt(mPrefs.getString(PREF_JMRI__SERVER_PORT, "20004"));
//        } catch (Exception ignore) {
//            return 20004;
//        }
//    }

// Imported from JED... remove or use later.
//    public boolean getPrefJmri_OneSocketPerThrottle() {
//        return mPrefs.getBoolean(PREF_JMRI__ONE_SOCKET_PER_THROTTLE, false);
//    }
//
//    public boolean getPrefJmri_ForceMultiThrottle() {
//        return mPrefs.getBoolean(PREF_JMRI__FORCE_MULTI_THROTTLE, true);
//    }

    @NonNull
    public String getData_ServerHostName() {
        return mPrefs.getString(PREF_DATA__SERVER_HOSTNAME, "");
    }

    public void setDataServerHostName(@NonNull String name) {
        synchronized (editLock()) {
            endEdit(startEdit().putString(PREF_DATA__SERVER_HOSTNAME, name));
        }
    }

    public int getData_ServerPort() {
        try {
            return Integer.parseInt(mPrefs.getString(PREF_DATA__SERVER_PORT, "20005"));
        } catch (Exception ignore) {
            return 20005;
        }
    }

    public void setData_ServerPort(int port) {
        synchronized (editLock()) {
            endEdit(startEdit().putString(PREF_DATA__SERVER_PORT, Integer.toString(port)));
        }
    }
}
