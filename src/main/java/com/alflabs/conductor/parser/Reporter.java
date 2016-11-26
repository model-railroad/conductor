package com.alflabs.conductor.parser;

import com.alflabs.conductor.util.Logger;

/**
 * Helper to output an error message. <br/>
 * This default implementation outputs to {@link System#out}.
 */
public class Reporter implements Logger {
    private Logger mLogger;
    private int mLastReportLine;

    public Reporter(Logger logger) {
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

    @Override
    public void log(String msg) {
        if (mLogger != null) {
            if (msg.length() > 0 && !msg.endsWith("\n")) {
                msg = msg + "\n";
            }
            mLogger.log(msg);
        }
    }
}
