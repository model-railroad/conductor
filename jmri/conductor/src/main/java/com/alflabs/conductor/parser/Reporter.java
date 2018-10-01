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

package com.alflabs.conductor.parser;

import com.alflabs.utils.ILogger;

/**
 * Helper to output an error message. <br/>
 * This default implementation outputs to {@link System#out}.
 */
public class Reporter {
    private static final String TAG = Reporter.class.getSimpleName();

    private ILogger mLogger;
    private int mLastReportLine;

    public Reporter(ILogger logger) {
        mLogger = logger;
    }

    public int getLastReportLine() {
        return mLastReportLine;
    }

    public void report(String line, int lineCount, String error) {
        mLastReportLine = lineCount;
        if (line != null && !line.isEmpty()) {
            log(String.format("Error at line %d: %s\n  Line %d: '%s'", lineCount, error, lineCount, line));
        } else {
            log(String.format("Error at line %d: %s", lineCount, error));
        }
    }

    public void log(String msg) {
        if (mLogger != null) {
            if (msg.length() > 0 && !msg.endsWith("\n")) {
                msg = msg + "\n";
            }
            mLogger.d(TAG, msg);
        }
    }
}
