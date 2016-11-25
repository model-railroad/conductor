package com.alflabs.conductor.script;

import com.alflabs.conductor.util.NowProvider;

/**
 * A timer defined by a script.
 * <p/>
 * Timers are initialized with a specific duration. Scripts only start or end the timer.
 * A timer is active once it has reached its expiration time and remains active until it
 * is either restart or ended.
 */
public class Timer implements IConditional {

    private final NowProvider mNowProvider;
    private final int mDurationSec;
    private long mEndTS;

    public Timer(int durationSec, NowProvider nowProvider) {
        mDurationSec = durationSec;
        mNowProvider = nowProvider;
        mEndTS = 0;
    }

    public int getDurationSec() {
        return mDurationSec;
    }

    public IIntFunction createFunctionStart() {
        return ignored -> mEndTS = now() + mDurationSec * 1000;
    }

    public IIntFunction createFunctionEnd() {
        return ignored -> mEndTS = 0;
    }

    @Override
    public boolean isActive() {
        return mEndTS != 0 && now() >= mEndTS;
    }

    public long now() {
        return mNowProvider.now();
    }
}
