package com.alfray.conductor.script;

import com.alfray.conductor.IJmriProvider;
import com.alfray.conductor.IJmriThrottle;

/**
 * A throttle defined by a script.
 * <p/>
 * The actual JMRI throttle is only assigned via the {@link #setup(IJmriProvider)} method.
 * <p/>
 * This throttle object keeps track of its state (speed, light/sound state) and only
 * uses its internal state to when providing values. JMRI is only used as a setter.
 */
public class Throttle {
    private final int mDccAddress;
    private IJmriThrottle mJmriThrottle;
    private int mSpeed;
    private boolean mSound;
    private boolean mLight;
    private IIntFunction mSpeedListener;

    /** Creates a new throttle for the given JMRI dcc address. */
    public Throttle(int dccAddress) {
        mDccAddress = dccAddress;
    }

    /** Initializes the underlying JMRI sensor. */
    public void setup(IJmriProvider provider) {
        mJmriThrottle = provider.getThrotlle(mDccAddress);
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

}
