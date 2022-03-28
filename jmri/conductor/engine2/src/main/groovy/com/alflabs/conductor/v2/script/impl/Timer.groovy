package com.alflabs.conductor.v2.script.impl

class Timer extends BaseActive {
    private final Optional<Timer> mBaseTimer
    private final int mDelay
    private boolean mIsStarted

    Timer(int delay) {
        mDelay = delay
        mBaseTimer = Optional.empty()
    }

    Timer(Timer baseTimer, int delay) {
        mDelay = delay
        mBaseTimer = Optional.of(baseTimer)
    }

    int getDelay() {
        return mDelay + (mBaseTimer.isPresent() ? mBaseTimer.get().getDelay() : 0)
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
