package com.alflabs.conductor.simulator;

import com.alflabs.conductor.script.Script;
import com.alflabs.conductor.script.Sensor;
import com.alflabs.conductor.script.Throttle;
import com.alflabs.conductor.script.Var;
import com.alflabs.utils.IClock;
import com.alflabs.utils.ILogger;

import javax.inject.Inject;
import java.util.Locale;

public class Simulator {
    private final String TAG = "Simu";
    private final ILogger mLogger;
    private final IClock mClock;
    private volatile Thread mCurrentThread;
    private volatile boolean mStopRequested;

    @Inject
    public Simulator(
            ILogger logger,
            IClock clock) {
        mLogger = logger;
        mClock = clock;
    }

    public void startAsync(Script script, String varName) {
        if (mCurrentThread != null) {
            mLogger.d(TAG, "Error: Can't start with a current simu thread running.");
            return;
        }

        Var scriptVar = script.getVar(varName);
        if (scriptVar == null) {
            mLogger.d(TAG, "Error: no simu var named " + varName);
            return;
        }

        String source = scriptVar.get();
        if (source == null || source.isEmpty()) {
            mLogger.d(TAG, "Error: simu var empty for " + varName);
            return;
        }

        mStopRequested = false;
        mCurrentThread = new Thread(() -> {
            asyncExec(script, source);
            mCurrentThread = null;
        });
        mCurrentThread.start();
    }

    public void stop() {
        if (mCurrentThread != null) {
            mLogger.d(TAG, "Stop requested");
            mStopRequested = true;
        } else {
            mLogger.d(TAG, "Error: Nothing to stop");
        }
    }

    public void join() throws InterruptedException {
        Thread t = mCurrentThread;
        if (t != null) {
            mLogger.d(TAG, "Join requested");
            t.join();
        } else {
            mLogger.d(TAG, "Error: Nothing to join");
        }
    }

    private void asyncExec(Script script, String source) {
        mLogger.d(TAG, "Started");

        String[] instructions = source.split("[;\r\n]");
        Throttle throttle = null;

        nextInstruction: for (String instruction : instructions) {
            if (mStopRequested) {
                mLogger.d(TAG, "Stop requested done");
                return;
            }

            if (instruction == null) {
                continue;
            }

            instruction = instruction.trim();
            if (instruction.isEmpty() || instruction.startsWith("#")) {
                continue;
            }

            String[] words = instruction.split("[ ]+");
            if (words.length < 1) {
                continue;
            }

            String keyword = words[0];
            String name;

            switch (keyword.toLowerCase(Locale.US)) {
            case "end":
                break nextInstruction;
            case "loco":
                name = words.length > 1 ? words[1] : "[missing]";
                throttle = script.getThrottle(name);
                if (throttle == null) {
                    mLogger.d(TAG, "Error: Unknown throttle '" + name + "' in '" + instruction + "'");
                }
                break;
            case "stop":
                if (throttle != null) {
                    throttle.setSpeed(0);
                } else {
                    mLogger.d(TAG, "Error: No throttle defined for '" + instruction + "'");
                }
                break;
            case "wait":
                if (words.length == 2 && words[1].endsWith("s")) {
                    waitOnTimer(instruction, words);

                } else if (words.length == 3 && words[1].equalsIgnoreCase("on")) {
                    waitOnSensorName(script, instruction, words);

                } else {
                    mLogger.d(TAG, "Error: Invalid format '" + instruction + "'");
                }
                break;
            case "set":
                if (words.length == 3) {
                    boolean state;
                    switch (words[1].toLowerCase(Locale.US)) {
                    case "on":
                        state = true;
                        break;
                    case "off":
                        state = false;
                        break;
                    default:
                        mLogger.d(TAG, "Error: Expected 'on' or 'off' in '" + instruction + "'");
                        break nextInstruction;
                    }

                    setSensorState(script, instruction, words, state);
                    break ;

                } else {
                    mLogger.d(TAG, "Error: Invalid format '" + instruction + "'");
                }
                break;
            default:
                mLogger.d(TAG, "Error: Unknown keyword '" + keyword + "' in '" + instruction + "'");
                break;
            }
        }

        mLogger.d(TAG, "Script end");
    }

    private void setSensorState(Script script, String instruction, String[] words, boolean state) {
        String name = words[2];
        Sensor sensor = script.getSensor(name);
        if (sensor == null) {
            mLogger.d(TAG, "Error: Unknown sensor '" + name + "' in '" + instruction + "'");
            return;
        }

        // This only works with the DevelopmentEntryPoint and not with a real JMRI provider.
        sensor.getJmriSensor().setActive(state);
    }

    private void waitOnTimer(String instruction, String[] words) {
        float seconds;
        try {
            seconds = Float.parseFloat(words[1].substring(0, words[1].length() - 1));
        } catch (NumberFormatException e) {
            mLogger.d(TAG, "Error: Invalid wait time in '" + instruction + "'");
            return;
        }

        long now = mClock.elapsedRealtime();
        long end = now + (long) (seconds * 1000);
        while (now < end) {
            boolean interrupted = false;
            try {
                mClock.sleep(Math.min(500, end - now));
            } catch (InterruptedException e) {
                interrupted = true;
            }
            now = mClock.elapsedRealtime();

            if (interrupted || mStopRequested) {
                mLogger.d(TAG, instruction + " interrupted.");
                break;
            }
        }
    }

    private void waitOnSensorName(Script script, String instruction, String[] words) {
        String name = words[2];
        Sensor sensor = script.getSensor(name);
        if (sensor == null) {
            mLogger.d(TAG, "Error: Unknown sensor '" + name + "' in '" + instruction + "'");
            return;
        }

        mLogger.d(TAG, "Wait for sensor '" + name + "'");
        while (!sensor.isActive()) {
            boolean interrupted = false;
            try {
                mClock.sleep(250);
            } catch (InterruptedException e) {
                interrupted = true;
            }

            if (interrupted || mStopRequested) {
                mLogger.d(TAG, instruction + " interrupted.");
                break;
            }
        }
    }
}
