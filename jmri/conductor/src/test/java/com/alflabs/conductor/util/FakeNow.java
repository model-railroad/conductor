package com.alflabs.conductor.util;

public class FakeNow extends Now {
    private long mNow;

    public FakeNow(long now) {
        mNow = now;
    }

    public void setNow(long now) {
        mNow = now;
    }

    public void add(long now) {
        mNow += now;
    }

    @Override
    public long now() {
        return mNow;
    }

    @Override
    public void sleep(long sleepTimeMs) {
        if (sleepTimeMs > 0) {
            add(sleepTimeMs);
        }
    }
}
