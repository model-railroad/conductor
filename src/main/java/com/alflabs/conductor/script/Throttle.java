package com.alflabs.conductor.script;

import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.IJmriThrottle;

/**
 * A throttle defined by a script.
 * <p/>
 * The actual JMRI throttle is only assigned via the {@link #setup(IJmriProvider)} method.
 * <p/>
 * This throttle object keeps track of its state (speed, light/sound state) and only
 * uses its internal state to when providing values. JMRI is only used as a setter.
 */
public class Throttle {
    private int mDccAddress;
    private IJmriThrottle mJmriThrottle;
    private int mSpeed;
    private boolean mSound;
    private boolean mLight;
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
        // TODO support FN
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
    }

    /** Creates a new throttle for the given JMRI dcc address. */
    public Throttle(int dccAddress) {
        mDccAddress = dccAddress;
    }

    /** Initializes the underlying JMRI sensor. */
    public void setup(IJmriProvider provider) {
        mJmriThrottle = provider.getThrotlle(mDccAddress);
    }

    public void setDccAddress(int dccAddress, IJmriProvider provider) {
        mDccAddress = dccAddress;
        setup(provider);
    }

    public int getDccAddress() {
        return mDccAddress;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public void setSpeed(int speed) {
        mSpeed = speed;
        if (mJmriThrottle != null) {
            mJmriThrottle.setSpeed(speed);
        }
        if (mSpeedListener != null) {
            try {
                mSpeedListener.accept(mSpeed);
            } catch (Throwable ignore) {}
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
                if (mJmriThrottle != null) {
                    mJmriThrottle.horn();
                }
            };
        case SOUND:
            return on -> {
                mSound = on != 0;
                if (mJmriThrottle != null) {
                    mJmriThrottle.setSound(mSound);
                }
            };
        case LIGHT:
            return on -> {
                mLight = on != 0;
                if (mJmriThrottle != null) {
                    mJmriThrottle.setLight(mLight);
                }
            };
        }
        throw new IllegalArgumentException();
    }

    public IIntFunction createFnFunction(int fn) {
        return on -> {
            boolean state = on != 0;
            if (mJmriThrottle != null) {
                mJmriThrottle.triggerFunction(fn, state);
            }
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
}
