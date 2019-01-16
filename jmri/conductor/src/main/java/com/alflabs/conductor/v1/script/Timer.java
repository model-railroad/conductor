/*
 * Project: Conductor
 * Copyright (C) 2017 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alflabs.conductor.v1.script;

import com.alflabs.conductor.util.EventLogger;
import com.alflabs.utils.IClock;
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

    private final String mTimerName;
    private final IClock mClock;
    private final EventLogger mEventLogger;
    private final int mDurationSec;
    private long mEndTS;
    private boolean mActivated;

    /**
     * Possible keywords for a timer function.
     * Must match IIntFunction in the {@link Timer} implementation.
     */
    public enum Function {
        /** Starts (or restarts) the timer countdown from "now". */
        START,
        /** Resets the timer so that it does not activate. */
        END
    }

    public Timer(int durationSec,
                 String timerName,
                 @Provided IClock clock,
                 @Provided EventLogger eventLogger) {
        mDurationSec = durationSec;
        mTimerName = timerName;
        mClock = clock;
        mEventLogger = eventLogger;
        mEndTS = 0;
    }

    public int getDurationSec() {
        return mDurationSec;
    }

    public IIntFunction createFunction(Function function) {
        switch (function) {
        case START:
            return ignored -> start();
        case END:
            return ignored -> reset();
        }
        throw new IllegalArgumentException();
    }

    @Override
    public boolean isActive() {
        if (!mActivated) {
            mActivated = mEndTS != 0 && now() >= mEndTS;
            if (mActivated) {
                mEventLogger.logAsync(EventLogger.Type.Timer, mTimerName, "activated");
            }
        }
        return mActivated;
    }

    private void start() {
        mEndTS = now() + mDurationSec * 1000;
        mEventLogger.logAsync(EventLogger.Type.Timer, mTimerName, "start:" + mDurationSec);
    }

    @Override
    public void reset() {
        mEndTS = 0;
        mActivated = false;
        mEventLogger.logAsync(EventLogger.Type.Timer, mTimerName, "reset");
    }

    private long now() {
        return mClock.elapsedRealtime();
    }

}
