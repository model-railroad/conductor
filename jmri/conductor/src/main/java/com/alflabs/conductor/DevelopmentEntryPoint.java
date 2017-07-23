package com.alflabs.conductor;

import com.alflabs.conductor.util.Logger;
import com.google.common.truth.Truth;

import java.util.concurrent.atomic.AtomicBoolean;

/** Entry point controlled for development purposes using a fake no-op JMRI interface. */
public class DevelopmentEntryPoint {

    public static void main(String[] args) {
        FakeJmriProvider jmriProvider = new FakeJmriProvider();
        AtomicBoolean keepRunning = new AtomicBoolean(true);
        EntryPoint entryPoint = new EntryPoint() {
            @Override
            protected void onStopAction() {
                super.onStopAction();
                keepRunning.set(false);
            }
        };
        String filePath = "src/test/resources/v2/script_pa+bl_9c.txt";
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
        @Override
        public void log(String msg) {
            System.out.println(msg);
        }

        @Override
        public IJmriThrottle getThrotlle(int dccAddress) {
            return new IJmriThrottle() {
                @Override
                public void setSpeed(int speed) {
                    log(String.format("[%d] Speed: %d\n", dccAddress, speed));
                }

                @Override
                public void setSound(boolean on) {
                    log(String.format("[%d] Sound: %s\n", dccAddress, on));
                }

                @Override
                public void setLight(boolean on) {
                    log(String.format("[%d] Light: %s\n", dccAddress, on));
                }

                @Override
                public void horn() {
                    log(String.format("[%d] Horn\n", dccAddress));
                }

                @Override
                public void triggerFunction(int function, boolean on) {
                    log(String.format("[%d] F%d: %s\n", dccAddress, function, on));
                }

                @Override
                public int getDccAddress() {
                    return dccAddress;
                }
            };
        }

        @Override
        public IJmriSensor getSensor(String systemName) {
            return () -> false;
        }

        @Override
        public IJmriTurnout getTurnout(String systemName) {
            return normal -> {
                log(String.format("[%s] Turnout: %s\n", systemName, normal ? "Normal" : "Reverse"));
            };
        }
    }

}
