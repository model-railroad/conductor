package com.alfray.conductor.util;

/**
 * Helper around {@link System#currentTimeMillis()} to be able to override it
 * during testing.
 */
public class NowProvider {
    /** Returns the current time in milliseconds. */
    public long now() {
        return System.currentTimeMillis();
    }
}
