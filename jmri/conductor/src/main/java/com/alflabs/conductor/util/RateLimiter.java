package com.alflabs.conductor.util;

public class RateLimiter {

    private final Now mNow;
    private final long mTargetTimeMs;
    private long mLastTimeMs;

    public RateLimiter(float frequencyHz, Now now) {
        mNow = now;
        mTargetTimeMs = (long) (1000.0f / frequencyHz);
    }

    public void limit() {
        // Iteration N:  ------------->|  pause  |-> (end time) ------>
        // Iteration N+1 ---actual------>| pause |-> (end time) ------>
        if (mLastTimeMs > 0) {
            // Actual time (ms) elapsed since the last mLastTimeMs checkpoint.
            long actual = mNow.now() - mLastTimeMs;
            // Assuming "actual" time was less than target, how long to sleep?
            long pause = mTargetTimeMs - actual;
            mNow.sleep(pause);
        }
        // Checkpoint. This should occur every mTargetTimeMs.
        mLastTimeMs = mNow.now();
    }
}
