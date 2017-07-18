package com.alflabs.conductor.util;

import javax.inject.Inject;

/**
 * Helper around {@link System#currentTimeMillis()} to be able to override it
 * during testing.
 */
public class Now {

    @Inject
    public Now() {}

    /** Returns the current time in milliseconds. */
    public long now() {
        return System.currentTimeMillis();
    }

    /** Sleeps for the requested time in milliseconds if > 0. */
    public void sleep(long sleepTimeMs) {
        if (sleepTimeMs > 0) {
            try {
                Thread.sleep(sleepTimeMs);
            } catch (InterruptedException ignore) {}
        }
    }

}
