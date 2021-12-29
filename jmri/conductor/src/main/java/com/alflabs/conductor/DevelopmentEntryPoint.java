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

import com.alflabs.conductor.jmri.FakeJmriProvider;
import com.alflabs.conductor.v1.simulator.Simulator;
import com.alflabs.utils.ILogger;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.truth.Truth.assertThat;

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
        String filePath = "src/test/resources/v2/script_v34_8736+1840+BL.txt";
        boolean parsed = entryPoint.setup(jmriProvider, filePath);
        assertThat(parsed).isTrue();
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

}
