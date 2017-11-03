package com.alflabs.conductor;

import com.alflabs.conductor.simulator.Simulator;
import com.alflabs.conductor.util.Logger;
import com.alflabs.utils.ILogger;
import com.google.common.truth.Truth;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** Entry point controlled for development purposes using a fake no-op JMRI interface. */
public class DevelopmentEntryPoint {

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
                return new Simulator(logger, component.getNow());
            }
        };
        String filePath = "src/test/resources/v2/script_pa+bl_11.txt";
        boolean parsed = entryPoint.setup(jmriProvider, filePath);
        Truth.assertThat(parsed).isTrue();
        if (parsed) {
            Thread thread = new Thread(() -> mainLoop(jmriProvider, keepRunning, entryPoint), "MainLoop");
            thread.start();
        }
    }

    private static void mainLoop(Logger logger, AtomicBoolean keepRunning, EntryPoint entryPoint) {
        logger.log("[Main] Start thread");
        while (keepRunning.get()) {
            entryPoint.handle();
        }
        logger.log("[Main] End thread");
    }

    private static class FakeJmriProvider implements IJmriProvider {
        final Map<String, IJmriSensor> mSensors = new TreeMap<>();

        @Override
        public void log(String msg) {
            System.out.println(msg);
        }

        @Override
        public IJmriThrottle getThrotlle(int dccAddress) {
            return new IJmriThrottle() {
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
            };
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
            return normal -> {
                log(String.format("[%s] Turnout: %s", systemName, normal ? "Normal" : "Reverse"));
            };
        }
    }

}
