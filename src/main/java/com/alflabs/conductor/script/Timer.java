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

    /**
     * Possible keywords for a timer function.
     * Must match IIntFunction in the {@link Timer} implementation.
     */
    public enum TimerFunction {
        START,
        END
    }

    public Timer(int durationSec, NowProvider nowProvider) {
        mDurationSec = durationSec;
        mNowProvider = nowProvider;
        mEndTS = 0;
    }

    public int getDurationSec() {
        return mDurationSec;
    }

    public IIntFunction createFunction(TimerFunction function) {
        switch (function) {
        case START:
            return ignored -> mEndTS = now() + mDurationSec * 1000;
        case END:
            return ignored -> mEndTS = 0;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public boolean isActive() {
        return mEndTS != 0 && now() >= mEndTS;
    }

    public long now() {
        return mNowProvider.now();
    }

}
