/*
 * Project: Conductor
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

package com.alflabs.conductor;

import com.alflabs.conductor.simulator.Simulator;
import com.alflabs.utils.ILogger;
import com.google.common.truth.Truth;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** Entry point controlled for development purposes using a fake no-op JMRI interface. */
public class DevelopmentEntryPoint {
    private static final String TAG = DevelopmentEntryPoint.class.getSimpleName();

    public static void main(String[] args) {
        FakeJmriProvider jmriProvider = new FakeJmriProvider();
        AtomicBoolean keepRunning = new AtomicBoolean(true);
        ILogger logger = new ILogger() {
            @Override
            public void d(String tag, String message) {
                System.out.println("[" + tag + "] " + message);
            }

            @Override
            public void d(String tag, String message, Throwable tr) {
                System.out.println("[" + tag + "] " + message + ": " + tr);
            }
        };
        EntryPoint entryPoint = new EntryPoint() {
            @Override
            protected void onStopAction() {
                super.onStopAction();
                keepRunning.set(false);
            }

            @Override
            protected Simulator getSimulator(IConductorComponent component) {
                return new Simulator(logger, component.getClock());
            }
        };
        String filePath = "src/test/resources/v2/script_v31_804+506+BL_sat.txt";
        boolean parsed = entryPoint.setup(jmriProvider, filePath);
        Truth.assertThat(parsed).isTrue();
        if (parsed) {
            Thread thread = new Thread(() -> mainLoop(jmriProvider, keepRunning, entryPoint), "MainLoop");
            thread.start();
        }
    }

    private static void mainLoop(ILogger logger, AtomicBoolean keepRunning, EntryPoint entryPoint) {
        logger.d(TAG, "Start thread");
        while (keepRunning.get()) {
            entryPoint.handle();
        }
        logger.d(TAG, "End thread");
    }

    public static class FakeJmriProvider implements IJmriProvider {
        final Map<String, IJmriSensor> mSensors = new TreeMap<>();
        final Map<String, IJmriTurnout> mTurnouts = new TreeMap<>();
        final Map<Integer, IJmriThrottle> mThrottles = new TreeMap<>();

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

        @Override
        public IJmriThrottle getThrotlle(int dccAddress) {
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

        @Override
        public IJmriSensor getSensor(String systemName) {
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
                    return systemName + ": " + Boolean.toString(mActive);
                }
            });
        }

        @Override
        public IJmriTurnout getTurnout(String systemName) {
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

}
