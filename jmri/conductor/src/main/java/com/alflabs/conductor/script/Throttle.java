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

package com.alflabs.conductor.script;

import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.IJmriThrottle;
import com.alflabs.conductor.util.Logger;
import com.alflabs.kv.IKeyValue;
import com.alflabs.manifest.Prefix;
import com.alflabs.utils.IClock;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * A throttle defined by a script.
 * <p/>
 * The actual JMRI throttle is only assigned via the {@link #onExecStart()} method.
 * <p/>
 * This throttle object keeps track of its state (speed, light/sound state) and only
 * uses its internal state to when providing values. JMRI is only used as a setter.
 */
@AutoFactory(allowSubclasses = true)
public class Throttle implements IExecEngine {
    private final List<Integer> mDccAddresses = new ArrayList<>();
    private final List<IJmriThrottle> mJmriThrottles = new ArrayList<>();
    private final IClock mClock;
    private final Logger mLogger;
    private final IJmriProvider mJmriProvider;
    private final IKeyValue mKeyValue;

    private int mSpeed;
    private boolean mSound;
    private boolean mLight;
    private int mRepeatSpeedSeconds;
    private long mLastJmriTS;
    private IIntFunction mSpeedListener;

    /**
     * Possible keywords for a throttle condition.
     * Must match IConditional in the {@link Throttle} implementation.
     */
    public enum Condition {
        FORWARD,
        REVERSE,
        STOPPED,
        SOUND,
        LIGHT,
    }

    /**
     * Possible keywords for a throttle action.
     * Must match IIntFunction in the {@link Throttle} implementation.
     */
    public enum Function {
        FORWARD,
        REVERSE,
        STOP,
        HORN,
        SOUND,
        LIGHT,
        REPEAT,
    }

    /** Creates a new throttle for one or more DCC addresses. */
    @Inject
    public Throttle(
            List<Integer> dccAddresses,
            @Provided IClock clock,
            @Provided Logger logger,
            @Provided IJmriProvider jmriProvider,
            @Provided IKeyValue keyValue) {
        mClock = clock;
        mLogger = logger;
        mJmriProvider = jmriProvider;
        mKeyValue = keyValue;
        mDccAddresses.addAll(dccAddresses);
    }

    /** Initializes the underlying JMRI throttles. */
    @Override
    public void onExecStart() {
        mJmriThrottles.clear();
        for (Integer dccAddress : mDccAddresses) {
            IJmriThrottle throtlle = mJmriProvider.getThrotlle(dccAddress);
            if (throtlle != null) {
                mJmriThrottles.add(throtlle);
                updateKV(throtlle.getDccAddress(), getSpeed());
            }
        }
    }

    public void setDccAddress(int dccAddress) {
        mDccAddresses.clear();
        mDccAddresses.add(dccAddress);
        onExecStart();
    }

    public List<Integer> getDccAddresses() {
        return mDccAddresses;
    }

    public String getDccAddressesAsString() {
        StringBuilder sb = new StringBuilder();
        if (mDccAddresses.isEmpty()) {
            sb.append("Missing");
        } else {
            for (Integer dccAddress : mDccAddresses) {
                sb.append(dccAddress).append(' ');
            }
        }
        return sb.toString().trim();
    }

    /** The last speed set for this engine. */
    public int getSpeed() {
        return mSpeed;
    }

    /**
     * Repeats the current speed if the specified delay as expired between now and the
     * last command sent to JMRI for this throttle.
     * <p/>
     * The call does nothing if {@link #getRepeatSpeedSeconds()} <= 0.
     */
    public void repeatSpeed() {
        if (mRepeatSpeedSeconds < 1) {
            return;
        }
        long elapsedMs = mClock.elapsedRealtime() - mLastJmriTS;
        if (elapsedMs >= 1000 * mRepeatSpeedSeconds) {
            setSpeed(mSpeed);
        }
    }

    /** Delay in seconds after the last command sent to JMRI before repeating the current speed. */
    public int getRepeatSpeedSeconds() {
        return mRepeatSpeedSeconds;
    }

    /**
     * Sets the throttle speed and direction.
     * Speed 0 means stopped, a positive number for forward and a negative number for reverse.
     */
    public void setSpeed(int speed) {
        mSpeed = speed;
        for (IJmriThrottle jmriThrottle : mJmriThrottles) {
            try {
                jmriThrottle.setSpeed(speed);
            } catch (Throwable e) {
                mLogger.log("Throttle [" + getDccAddressesAsString() + "] setSpeed exception: " + e);
            }
            try {
                updateKV(jmriThrottle.getDccAddress(), speed);
            } catch (Throwable e) {
                mLogger.log("Throttle [" + getDccAddressesAsString() + "] getDccAddress exception: " + e);
            }
        }

        mLastJmriTS = mClock.elapsedRealtime();

        if (mSpeedListener != null) {
            try {
                mSpeedListener.accept(mSpeed);
            } catch (Throwable e) {
                mLogger.log("Throttle [" + getDccAddressesAsString() + "] mSpeedListener exception: " + e);
            }
        }
    }

    public void setSpeedListener(IIntFunction speedListener) {
        mSpeedListener = speedListener;
    }

    public IIntFunction createFunction(Function function) {
        switch (function) {
        case FORWARD:
            return speed -> setSpeed(Math.max(0, speed));
        case REVERSE:
            return speed -> setSpeed(-1 * Math.max(0, speed));
        case STOP:
            return speed -> setSpeed(0);
        case HORN:
            return on -> {
                for (IJmriThrottle jmriThrottle : mJmriThrottles) {
                    try {
                        jmriThrottle.horn();
                    } catch (Throwable e) {
                        mLogger.log("Throttle [" + getDccAddressesAsString() + "] horn exception: " + e);
                    }
                }
                mLastJmriTS = mClock.elapsedRealtime();
            };
        case SOUND:
            return on -> {
                mSound = on != 0;
                for (IJmriThrottle jmriThrottle : mJmriThrottles) {
                    try {
                        jmriThrottle.setSound(mSound);
                    } catch (Throwable e) {
                        mLogger.log("Throttle [" + getDccAddressesAsString() + "] setSound exception: " + e);
                    }
                }
                mLastJmriTS = mClock.elapsedRealtime();
            };
        case LIGHT:
            return on -> {
                mLight = on != 0;
                for (IJmriThrottle jmriThrottle : mJmriThrottles) {
                    try {
                        jmriThrottle.setLight(mLight);
                    } catch (Throwable e) {
                        mLogger.log("Throttle [" + getDccAddressesAsString() + "] setLight exception: " + e);
                    }
                }
                mLastJmriTS = mClock.elapsedRealtime();
            };
        case REPEAT:
            return on -> {
                // The value passed is the repeat delay in seconds.
                mRepeatSpeedSeconds = on;
            };
        }
        throw new IllegalArgumentException();
    }

    public IIntFunction createFnFunction(int fn) {
        return on -> {
            boolean state = on != 0;
            for (IJmriThrottle jmriThrottle : mJmriThrottles) {
                try {
                    jmriThrottle.triggerFunction(fn, state);
                } catch (Throwable e) {
                    mLogger.log("Throttle [" + getDccAddressesAsString() + "] triggerFunction exception: " + e);
                }
            }
            mLastJmriTS = mClock.elapsedRealtime();
        };
    }

    public IConditional createCondition(Condition condition) {
        switch (condition) {
        case FORWARD:
            return () -> mSpeed > 0;
        case REVERSE:
            return () -> mSpeed < 0;
        case STOPPED:
            return () -> mSpeed == 0;
        case SOUND:
            return () -> mSound;
        case LIGHT:
            return () -> mLight;
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void onExecHandle() {
        // no-op
    }

    public void eStop() {
        // Do a "soft" stop to speed 0. This also sets this object's state properly.
        setSpeed(0);

        // Ask JMRI to send an e-stop command to all throttles
        for (IJmriThrottle jmriThrottle : mJmriThrottles) {
            try {
                jmriThrottle.eStop();
            } catch (Throwable e) {
                mLogger.log("Throttle [" + getDccAddressesAsString() + "] eStop exception: " + e);
            }
        }
    }

    private void updateKV(int address, int speed) {
        mKeyValue.putValue(
                Prefix.DccThrottle + Integer.toString(address),
                Integer.toString(speed), true /*broadcast*/);
    }
}
