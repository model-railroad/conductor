package com.alfray.conductor.script;

import com.alfray.conductor.IJmriProvider;
import com.alfray.conductor.IJmriTurnout;

/**
 * A turnout defined by a script.
 * <p/>
 * The actual JMRI turnout is only assigned via the {@link #setup(IJmriProvider)} method.
 * <p/>
 * JMRI is only used as a setter. We don't use the internal turnout state.
 */
public class Turnout {

    private final String mJmriName;
    private IJmriTurnout mTurnout;

    /** Creates a new turnout for the given JMRI system name. */
    public Turnout(String jmriName) {
        mJmriName = jmriName;
    }

    /** Initializes the underlying JMRI turnout. */
    public void setup(IJmriProvider provider) {
        mTurnout = provider.getTurnout(mJmriName);
    }

    public IFunction.Int createFunctionNormal() {
        return ignored -> mTurnout.setTurnout(IJmriTurnout.NORMAL);
    }

    public IFunction.Int createFunctionReverse() {
        return ignored -> mTurnout.setTurnout(IJmriTurnout.REVERSE);
    }
}
