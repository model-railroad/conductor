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
    public enum ThrottleCondition {
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
    public enum ThrottleFunction {
        FORWARD,
        REVERSE,
        STOP,
        HORN,
        SOUND,
        LIGHT,
        // TODO support FN
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

    public IIntFunction createFunction(ThrottleFunction function) {
        switch (function) {
        case FORWARD:
            return createFunctionForward();
        case REVERSE:
            return createFunctionReverse();
        case STOP:
            return createFunctionStop();
        case HORN:
            return createFunctionHorn();
        case SOUND:
            return createFunctionSound();
        case LIGHT:
            return createFunctionLight();
        }
        throw new IllegalArgumentException();
    }

    public IIntFunction createFunctionStop() {
        return speed -> setSpeed(0);
    }

    public IIntFunction createFunctionForward() {
        return speed -> setSpeed(Math.max(0, speed));
    }

    public IIntFunction createFunctionReverse() {
        return speed -> setSpeed(-1 * Math.max(0, speed));
    }

    public IIntFunction createFunctionSound() {
        return on -> {
            mSound = on != 0;
            if (mJmriThrottle != null) {
                mJmriThrottle.setSound(mSound);
            }
        };
    }

    public IIntFunction createFunctionLight() {
        return on -> {
            mLight = on != 0;
            if (mJmriThrottle != null) {
                mJmriThrottle.setLight(mLight);
            }
        };
    }

    public IIntFunction createFunctionHorn() {
        return on -> {
            if (mJmriThrottle != null) {
                mJmriThrottle.horn();
            }
        };
    }

    public IConditional createIsStopped() {
        return () -> mSpeed == 0;
    }

    public IConditional createIsForward() {
        return () -> mSpeed > 0;
    }

    public IConditional createIsReverse() {
        return () -> mSpeed < 0;
    }

    public IConditional createIsSound() {
        return () -> mSound;
    }

    public IConditional createIsLight() {
        return () -> mLight;
    }


    public IConditional createCondition(ThrottleCondition condition) {
        switch (condition) {
        case FORWARD:
            return createIsForward();
        case REVERSE:
            return createIsReverse();
        case STOPPED:
            return createIsStopped();
        case SOUND:
            return createIsSound();
        case LIGHT:
            return createIsLight();
        }
        throw new IllegalArgumentException();
    }
}
