package com.alflabs.conductor.v2.script

class Timer extends BaseActive {
    private final int mDelay

    Timer(int delay) {
        this.mDelay = delay
    }

    int getDelay() {
        return mDelay
    }

    void setTriggered(boolean triggered) {
        setActive(triggered)
    }
}
