package com.alfray.conductor.script;

import com.alfray.conductor.IJmriProvider;
import com.alfray.conductor.IJmriTurnout;

/**
 * A turnout defined by a script.
 * <p/>
 * The actual JMRI turnout is only assigned via the {@link #setup(IJmriProvider)} method.
 * <p/>
 * JMRI is only used as a setter. We don't use the JMRI turnout state and instead cache
 * the last state set. The default state is normal.
 * <p/>
 * When used as a conditional, a turnout is true in its "normal" state and false
 * in reverse.
 */
public class Turnout implements IConditional {

    private final String mJmriName;
    private IJmriTurnout mTurnout;
    private boolean mIsNormal = true;

    /** Creates a new turnout for the given JMRI system name. */
    public Turnout(String jmriName) {
        mJmriName = jmriName;
    }

    /** Initializes the underlying JMRI turnout. */
    public void setup(IJmriProvider provider) {
        mTurnout = provider.getTurnout(mJmriName);
    }

    public IIntFunction createFunctionNormal() {
        return ignored -> setTurnout(IJmriTurnout.NORMAL);
    }

    public IIntFunction createFunctionReverse() {
        return ignored -> setTurnout(IJmriTurnout.REVERSE);
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
