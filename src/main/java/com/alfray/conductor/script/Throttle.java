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

    /** Creates a new throttle for the given JMRI dcc address. */
    public Throttle(int dccAddress) {
        mDccAddress = dccAddress;
    }

    /** Initializes the underlying JMRI sensor. */
    public void setup(IJmriProvider provider) {
        mJmriThrottle = provider.getThrotlle(mDccAddress);
    }

    public int getSpeed() {
        return mSpeed;
    }

    public void setSpeed(int speed) {
        mSpeed = speed;
        if (mJmriThrottle != null) {
            mJmriThrottle.setSpeed(speed);
        }
    }

    public IFunction.Int createFunctionStop() {
        return speed -> setSpeed(0);
    }

    public IFunction.Int createFunctionForward() {
        return speed -> setSpeed(Math.max(0, speed));
    }

    public IFunction.Int createFunctionReverse() {
        return speed -> setSpeed(-1 * Math.max(0, speed));
    }

    public IFunction.Int createFunctionSound() {
        return on -> {
            mSound = on != 0;
            if (mJmriThrottle != null) {
                mJmriThrottle.setSound(mSound);
            }
        };
    }

    public IFunction.Int createFunctionLight() {
        return on -> {
            mLight = on != 0;
            if (mJmriThrottle != null) {
                mJmriThrottle.setLight(mLight);
            }
        };
    }

    public IFunction.Int createFunctionHorn() {
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
