package com.alflabs.conductor.script;

import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.IJmriTurnout;
import com.alflabs.kv.IKeyValue;
import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;

import javax.inject.Inject;

/**
 * A turnout defined by a script.
 * <p/>
 * The actual JMRI turnout is only assigned via the {@link #onExecStart()} method.
 * <p/>
 * JMRI is only used as a setter. We don't use the JMRI turnout state and instead cache
 * the last state set. The default state is normal.
 * <p/>
 * When used as a conditional, a turnout is true in its "normal" state and false
 * in reverse.
 */
@AutoFactory(allowSubclasses = true)
public class Turnout implements IConditional, IExecEngine {

    private final String mJmriName;
    private final String mScriptName;
    private final IJmriProvider mJmriProvider;
    private final IKeyValue mKeyValue;

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
    @Inject
    public Turnout(
            String jmriName,
            String scriptName,
            @Provided IJmriProvider jmriProvider,
            @Provided IKeyValue keyValue) {
        mJmriName = jmriName;
        mScriptName = scriptName;
        mJmriProvider = jmriProvider;
        mKeyValue = keyValue;
    }

    /** Initializes the underlying JMRI turnout. */
    @Override
    public void onExecStart() {
        mTurnout = mJmriProvider.getTurnout(mJmriName);
        onExecHandle();
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

    @Override
    public void onExecHandle() {
        mKeyValue.putValue(mScriptName, mIsNormal ? "N" : "R", true /*broadcast*/);
    }
}
