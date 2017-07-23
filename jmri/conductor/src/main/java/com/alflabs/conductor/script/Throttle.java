package com.alflabs.conductor.script;

import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.IJmriThrottle;
import com.alflabs.kv.IKeyValue;
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
    private final IJmriProvider mJmriProvider;
    private final IKeyValue mKeyValue;

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

    /** Creates a new throttle for one or more DCC addresses. */
    @Inject
    public Throttle(
            List<Integer> dccAddresses,
            @Provided IJmriProvider jmriProvider,
            @Provided IKeyValue keyValue) {
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

    public String getDccAddresses() {
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

    public int getSpeed() {
        return mSpeed;
    }

    public void setSpeed(int speed) {
        mSpeed = speed;
        for (IJmriThrottle jmriThrottle : mJmriThrottles) {
            jmriThrottle.setSpeed(speed);
            updateKV(jmriThrottle.getDccAddress(), speed);
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
                for (IJmriThrottle jmriThrottle : mJmriThrottles) {
                    jmriThrottle.horn();
                }
            };
        case SOUND:
            return on -> {
                mSound = on != 0;
                for (IJmriThrottle jmriThrottle : mJmriThrottles) {
                    jmriThrottle.setSound(mSound);
                }
            };
        case LIGHT:
            return on -> {
                mLight = on != 0;
                for (IJmriThrottle jmriThrottle : mJmriThrottles) {
                    jmriThrottle.setLight(mLight);
                }
            };
        }
        throw new IllegalArgumentException();
    }

    public IIntFunction createFnFunction(int fn) {
        return on -> {
            boolean state = on != 0;
            for (IJmriThrottle jmriThrottle : mJmriThrottles) {
                jmriThrottle.triggerFunction(fn, state);
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

    @Override
    public void onExecHandle() {
        // no-op
    }

    private void updateKV(int address, int speed) {
        mKeyValue.putValue(
                "Throttle-" + Integer.toString(address),
                Integer.toString(speed), true /*broadcast*/);
    }
}
