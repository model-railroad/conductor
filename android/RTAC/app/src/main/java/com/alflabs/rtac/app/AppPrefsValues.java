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

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.alflabs.dagger.AppQualifier;
import com.alflabs.prefs.BasePrefsValues;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Locale;


@Singleton
public class AppPrefsValues extends BasePrefsValues {

    // The enum names here must match the ones in res/values/prefs_arrays.xml
    public enum BootAction {
        NO_ACTION,
        START_RTAC
    }

    public static final String PREF_SYSTEM__HIDE_NAVIGATION = "pref_system__hide_navigation";
    public static final String PREF_SYSTEM__BOOT_ACTION     = "pref_system__boot_action";
    public static final String PREF_SYSTEM__ENABLE_NSD      = "pref_system__enable_nsd";
    public static final String PREF_SYSTEM__ENABLE_NSD_IPV6 = "pref_system__enable_nsd_ipv6";
    public static final String PREF_SYSTEM__WIFI_SSID       = "pref_system__wifi_ssid";

    public static final String PREF_DATA__SERVER_HOSTNAME = "pref_data__server_hostname";
    public static final String PREF_DATA__SERVER_PORT     = "pref_data__server_port";

    public static final String PREF_JMRI__SERVER_HOSTNAME = "pref_jmri__server_hostname";
    public static final String PREF_JMRI__SERVER_PORT     = "pref_jmri__server_port";
    public static final String PREF_JMRI__THROTTLE_NAME   = "pref_jmri__throttle_name";

    public static final String PREF_CONDUCTOR__CONTROL_EMERGENCY_STOP = "pref_conductor__control_emergency_stop";
    public static final String PREF_CONDUCTOR__MONITOR_MOTION_SENSOR = "pref_conductor__monitor_motion_sensor";

    public static final String PREF_DEV__SIMUL_MOTION_SENSOR = "pref_dev__simul_motion_sensor";

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

    public boolean getSystem_EnableNsdIpv6() {
        return mPrefs.getBoolean(PREF_SYSTEM__ENABLE_NSD_IPV6, false);
    }

    public String getSystem_WifiSsid() {
        return mPrefs.getString(PREF_SYSTEM__WIFI_SSID, "");
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

    public String getJmri_ThrottleName() {
        return mPrefs.getString(PREF_JMRI__THROTTLE_NAME, "");
    }

    public void setJmri_ThrottleName(@NonNull String name) {
        synchronized (editLock()) {
            endEdit(startEdit().putString(PREF_JMRI__THROTTLE_NAME, name));
        }
    }

    @NonNull
    public String getJmri_ServerHostName() {
        return mPrefs.getString(PREF_JMRI__SERVER_HOSTNAME, "localhost");
    }

    public int getJmri_ServerPort() {
        try {
            return Integer.parseInt(mPrefs.getString(PREF_JMRI__SERVER_PORT, "20004"));
        } catch (Exception ignore) {
            return 20004;
        }
    }

    @NonNull
    public String getData_ServerHostName() {
        return mPrefs.getString(PREF_DATA__SERVER_HOSTNAME, "localhost");
    }

    public void setData_ServerHostName(@NonNull String name) {
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

    public boolean getConductor_ControlEmergencyStop() {
        return mPrefs.getBoolean(PREF_CONDUCTOR__CONTROL_EMERGENCY_STOP, true);
    }

    public boolean getConductor_MonitorMotionSensor() {
        return mPrefs.getBoolean(PREF_CONDUCTOR__MONITOR_MOTION_SENSOR, false);
    }

    public boolean getDev_SimulMotionSensor() {
        return mPrefs.getBoolean(PREF_DEV__SIMUL_MOTION_SENSOR, false);
    }
}
