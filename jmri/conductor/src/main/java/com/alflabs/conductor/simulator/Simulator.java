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

package com.alflabs.conductor.simulator;

import com.alflabs.conductor.script.Script;
import com.alflabs.conductor.script.Sensor;
import com.alflabs.conductor.script.Throttle;
import com.alflabs.conductor.script.Var;
import com.alflabs.utils.IClock;
import com.alflabs.utils.ILogger;

import javax.inject.Inject;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Simulator {
    private final String TAG = "Simu";
    private final ILogger mLogger;
    private final IClock mClock;
    private Map<String, Thread> mThreads = new ConcurrentHashMap<>(2, 0.75f, 2);
    private volatile boolean mStopRequested;

    @Inject
    public Simulator(
            ILogger logger,
            IClock clock) {
        mLogger = logger;
        mClock = clock;
    }

    public void startAsync(Script script, String varName) {
        final String tag = TAG + " " + varName.replace("simulation-", "");
        Thread thread = mThreads.get(varName);
        if (thread != null) {
            mLogger.d(tag, "Error: Can't start with a current simu thread running.");
            return;
        }

        Var scriptVar = script.getVar(varName);
        if (scriptVar == null) {
            mLogger.d(tag, "Error: no simu var named " + varName);
            return;
        }

        String source = scriptVar.get();
        if (source == null || source.isEmpty()) {
            mLogger.d(tag, "Error: simu var empty for " + varName);
            return;
        }

        mStopRequested = false;
        thread = new Thread(() -> {
            asyncExec(tag, script, source);
            mThreads.remove(varName);
        });
        mThreads.put(varName, thread);
        thread.start();
    }

    public void stop() {
        if (mThreads.isEmpty()) {
            mLogger.d(TAG, "Warning: Nothing to stop");
        } else {
            mLogger.d(TAG, "Stop requested");
            mStopRequested = true;
        }
    }

    public void join() throws InterruptedException {
        if (mThreads.isEmpty()) {
            mLogger.d(TAG, "Warning: Nothing to join");
        } else {
            mLogger.d(TAG, "Join requested");
            for (Thread thread : mThreads.values()) {
                thread.join();
            }
        }
    }

    private void asyncExec(String tag, Script script, String source) {
        mLogger.d(tag, "Started");

        String[] instructions = source.split("[;\r\n]");
        Throttle throttle = null;

        nextInstruction: for (String instruction : instructions) {
            if (mStopRequested) {
                mLogger.d(tag, "Stop requested done");
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
            case "throttle":
                name = words.length > 1 ? words[1] : "[missing]";
                throttle = script.getThrottle(name);
                if (throttle == null) {
                    mLogger.d(tag, "Error: Unknown throttle '" + name + "' in '" + instruction + "'");
                }
                break;
            case "stop":
                if (throttle != null) {
                    throttle.setSpeed(0);
                } else {
                    mLogger.d(tag, "Error: No throttle defined for '" + instruction + "'");
                }
                break;
            case "wait":
                if (words.length == 2 && words[1].endsWith("s")) {
                    waitOnTimer(tag, instruction, words);

                } else if (words.length == 3 && words[1].equalsIgnoreCase("on")) {
                    waitOnSensorName(tag, script, instruction, words);

                } else {
                    mLogger.d(tag, "Error: Invalid format '" + instruction + "'");
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
                        mLogger.d(tag, "Error: Expected 'on' or 'off' in '" + instruction + "'");
                        break nextInstruction;
                    }

                    setSensorState(tag, script, instruction, words, state);
                    break ;

                } else {
                    mLogger.d(tag, "Error: Invalid format '" + instruction + "'");
                }
                break;
            default:
                mLogger.d(tag, "Error: Unknown keyword '" + keyword + "' in '" + instruction + "'");
                break;
            }
        }

        mLogger.d(tag, "Script end");
    }

    private void setSensorState(String tag, Script script, String instruction, String[] words, boolean state) {
        String name = words[2];
        Sensor sensor = script.getSensor(name);
        if (sensor == null) {
            mLogger.d(tag, "Error: Unknown sensor '" + name + "' in '" + instruction + "'");
            return;
        }

        // This only works with the DevelopmentEntryPoint and not with a real JMRI provider.
        mLogger.d(tag, "Set sensor '" + name + "' to " + (state ? "ON" : "OFF"));
        sensor.getJmriSensor().setActive(state);
    }

    private void waitOnTimer(String tag, String instruction, String[] words) {
        float seconds;
        try {
            seconds = Float.parseFloat(words[1].substring(0, words[1].length() - 1));
        } catch (NumberFormatException e) {
            mLogger.d(tag, "Error: Invalid wait time in '" + instruction + "'");
            return;
        }

        long now = mClock.elapsedRealtime();
        long end = now + (long) (seconds * 1000);
        while (now < end) {
            boolean interrupted = false;
            try {
                mClock.sleepWithInterrupt(Math.min(500, end - now));
            } catch (InterruptedException e) {
                interrupted = true;
            }
            now = mClock.elapsedRealtime();

            if (interrupted || mStopRequested) {
                mLogger.d(tag, instruction + " interrupted.");
                break;
            }
        }
    }

    private void waitOnSensorName(String tag, Script script, String instruction, String[] words) {
        String name = words[2];
        Sensor sensor = script.getSensor(name);
        if (sensor == null) {
            mLogger.d(TAG, "Error: Unknown sensor '" + name + "' in '" + instruction + "'");
            return;
        }

        mLogger.d(tag, "Wait for sensor '" + name + "'");
        while (!sensor.isActive()) {
            boolean interrupted = false;
            try {
                mClock.sleepWithInterrupt(250);
            } catch (InterruptedException e) {
                interrupted = true;
            }

            if (interrupted || mStopRequested) {
                mLogger.d(tag, instruction + " interrupted.");
                break;
            }
        }
    }
}
