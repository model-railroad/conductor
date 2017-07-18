package com.alflabs.conductor.script;

import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.IJmriTurnout;

/**
 * A turnout defined by a script.
 * <p/>
 * The actual JMRI turnout is only assigned via the {@link #onExecStart(IJmriProvider)} method.
 * <p/>
 * JMRI is only used as a setter. We don't use the JMRI turnout state and instead cache
 * the last state set. The default state is normal.
 * <p/>
 * When used as a conditional, a turnout is true in its "normal" state and false
 * in reverse.
 */
public class Turnout implements IConditional, IExecStart {

    private final String mJmriName;
    private IJmriTurnout mTurnout;
    private boolean mIsNormal = true;

    /**
     * Possible keywords for a turnout function.
     * Must match IIntFunction in the {@link Turnout} implementation.
     */
    public enum Function {
        NORMAL,
        REVERSE
    }

    /** Creates a new turnout for the given JMRI system name. */
    public Turnout(String jmriName) {
        mJmriName = jmriName;
    }

    /** Initializes the underlying JMRI turnout. */
    @Override
    public void onExecStart(IJmriProvider provider) {
        mTurnout = provider.getTurnout(mJmriName);
    }

    public IIntFunction createFunction(Function function) {
        switch (function) {
        case NORMAL:
            return ignored -> setTurnout(IJmriTurnout.NORMAL);
        case REVERSE:
            return ignored -> setTurnout(IJmriTurnout.REVERSE);
        }
        throw new IllegalArgumentException();
    }

    private void setTurnout(boolean normal) {
        mIsNormal = normal;
        if (mTurnout != null) {
            mTurnout.setTurnout(normal);
        }
    }

    @Override
    public boolean isActive() {
        return mIsNormal;
    }
}
