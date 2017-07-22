package com.alflabs.conductor;

import com.google.common.truth.Truth;

/** Entry point controlled for development purposes using a fake no-op JMRI interface. */
public class DevelopmentEntryPoint {

    public static void main(String[] args) {
        EntryPoint ep = new EntryPoint();
        String filePath = "src/test/resources/v2/script_pa+bl_9c.txt";
        Truth.assertThat(ep.setup(new FakeJmriProvider(), filePath)).isTrue();
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
            return new IJmriSensor() {
                @Override
                public boolean isActive() {
                    return false;
                }
            };
        }

        @Override
        public IJmriTurnout getTurnout(String systemName) {
            return new IJmriTurnout() {
                @Override
                public void setTurnout(boolean normal) {
                    log(String.format("[%s] Turnout: %s\n", systemName, normal ? "Normal" : "Reverse"));
                }
            };
        }
    }

}
