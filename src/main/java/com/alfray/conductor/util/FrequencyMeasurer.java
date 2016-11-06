package com.alfray.conductor.util;

public class FrequencyMeasurer {

    private final NowProvider mNowProvider;
    private long mLastPingMs;
    private long mDelayMs;

    public FrequencyMeasurer(NowProvider nowProvider) {
        mNowProvider = nowProvider;
    }

    public void ping() {
        long now = mNowProvider.now();
        if (mLastPingMs != 0) {
            long delay = now - mLastPingMs;
            // simple averaging: 1/3rd last delay, 2/3rd new delay
            mDelayMs = mDelayMs <= 0 ? delay : (mDelayMs + 2 * delay) / 3;
        }
        mLastPingMs = now;
    }

    public float getFrequency() {
        return mDelayMs <= 0 ? 0 : (1000.0f / mDelayMs);
    }
}
