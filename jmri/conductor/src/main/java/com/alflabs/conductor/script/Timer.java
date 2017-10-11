package com.alflabs.conductor.script;

import com.alflabs.conductor.util.Now;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

/**
 * A timer defined by a script.
 * <p/>
 * Timers are initialized with a specific duration. Scripts only start or end the timer.
 * A timer is active once it has reached its expiration time and remains active until it
 * is either restart or ended.
 */
@AutoFactory(allowSubclasses = true)
public class Timer implements IConditional, IResettable {

    private final Now mNow;
    private final int mDurationSec;
    private long mEndTS;

    /**
     * Possible keywords for a timer function.
     * Must match IIntFunction in the {@link Timer} implementation.
     */
    public enum Function {
        START,
        END
    }

    public Timer(int durationSec, @Provided Now now) {
        mDurationSec = durationSec;
        mNow = now;
        mEndTS = 0;
    }

    public int getDurationSec() {
        return mDurationSec;
    }

    public IIntFunction createFunction(Function function) {
        switch (function) {
        case START:
            return ignored -> mEndTS = now() + mDurationSec * 1000;
        case END:
            return ignored -> reset();
        }
        throw new IllegalArgumentException();
    }

    @Override
    public boolean isActive() {
        return mEndTS != 0 && now() >= mEndTS;
    }

    @Override
    public void reset() {
        mEndTS = 0;
    }

    private long now() {
        return mNow.now();
    }

}
