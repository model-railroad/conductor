/*
 * Project: Conductor
 * Copyright (C) 2022 alf.labs gmail com,
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

package com.alflabs.conductor.jmri;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;

import java.util.Map;
import java.util.TreeMap;

public class FakeJmriProvider implements IJmriProvider {
    private static final String TAG = FakeJmriProvider.class.getSimpleName();

    protected final Map<String, IJmriSensor> mSensors = new TreeMap<>();
    protected final Map<String, IJmriTurnout> mTurnouts = new TreeMap<>();
    protected final Map<Integer, IJmriThrottle> mThrottles = new TreeMap<>();

    // Interface ILogger
    @Override
    public void d(String tag, String message) {
        System.out.println(tag + ": " + message);
    }

    // Interface ILogger
    @Override
    public void d(String tag, String message, Throwable tr) {
        System.out.println(tag + ": " + message + ": " + tr);
    }

    private void log(String msg) {
        d(TAG, msg);
    }

    @Null
    @Override
    public IJmriThrottle getThrottle(int dccAddress) {
        return mThrottles.computeIfAbsent(dccAddress, key -> new IJmriThrottle() {
            @Override
            public void eStop() {
                log(String.format("[%d] E-Stop", dccAddress));
            }

            @Override
            public void setSpeed(int speed) {
                log(String.format("[%d] Speed: %d", dccAddress, speed));
            }

            @Override
            public void setSound(boolean on) {
                log(String.format("[%d] Sound: %s", dccAddress, on));
            }

            @Override
            public void setLight(boolean on) {
                log(String.format("[%d] Light: %s", dccAddress, on));
            }

            @Override
            public void horn() {
                log(String.format("[%d] Horn", dccAddress));
            }

            @Override
            public void triggerFunction(int function, boolean on) {
                log(String.format("[%d] F%d: %s", dccAddress, function, on));
            }

            @Override
            public int getDccAddress() {
                return dccAddress;
            }
        });
    }

    @Null
    @Override
    public IJmriSensor getSensor(@NonNull String systemName) {
        return mSensors.computeIfAbsent(systemName, key -> new IJmriSensor() {
            boolean mActive = false;

            @Override
            public boolean isActive() {
                return mActive;
            }

            @Override
            public void setActive(boolean active) {
                mActive = active;
            }

            @Override
            public String toString() {
                return systemName + ": " + mActive;
            }
        });
    }

    @Null
    @Override
    public IJmriTurnout getTurnout(@NonNull String systemName) {
        return mTurnouts.computeIfAbsent(systemName, key -> new IJmriTurnout() {
            private boolean mKnownState = true;

            @Override
            public boolean isNormal() {
                return mKnownState;
            }

            @Override
            public void setTurnout(boolean normal) {
                mKnownState = normal;
                log(String.format("[%s] Turnout: %s", systemName, normal ? "Normal" : "Reverse"));
            }
        });
    }
}
