package com.alflabs.conductor.v2.script

class Timer extends BaseActive {
    private final int mDelay
    private boolean mIsStarted

    Timer(int delay) {
        mDelay = delay
    }

    int getDelay() {
        return mDelay
    }

    void start() {
        mIsStarted = true
    }

    void reset() {
        mIsStarted = false
    }

    boolean isStarted() {
        return mIsStarted
    }
}
