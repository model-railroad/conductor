package com.alflabs.conductor.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogException {
    public static void logException(Logger logger, Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        logger.log(sw.toString());
    }
}
