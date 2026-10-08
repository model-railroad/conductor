/*
 * Project: Conductor
 * Copyright (C) 2026 alf.labs gmail com,
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

package com.alflabs.conductor.util;

import com.alflabs.utils.ILogger;

import java.util.List;

/**
 * A wrapper that executes ProcessBuilder.start() in a non-blocking thread.
 * <p>
 * Note: do not import this directly. Import IProcessBuilderAsync via the ProcessBuilderModule
 * to properly get the fake override during tests.
 */
public class ProcessBuilderAsync implements IProcessBuilderAsync {
    private static final String TAG = ProcessBuilderAsync.class.getSimpleName();
    private final ILogger mLogger;

    public ProcessBuilderAsync(ILogger logger) {
        mLogger = logger;
    }

    public void execAsync(List<String> command) {
        Thread t = new Thread(() -> {
            try {
                // Redirects IO to current process stdout/stderr to prevent OS buffer deadlocks
                ProcessBuilder pb = new ProcessBuilder(command);
                Process process = pb
                        .inheritIO()
                        .start();

                int exitCode = process.waitFor();
                mLogger.d(TAG, "EXEC finished with exit code: " + exitCode);
            } catch (Exception e) {
                mLogger.d(TAG, "EXEC failed to execute script:", e);
            }
        });
                
        t.start();
    }
}
