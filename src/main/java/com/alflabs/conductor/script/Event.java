package com.alflabs.conductor.script;

import com.alflabs.conductor.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one event with its condition list, its action list and an "execution engine".
 */
public class Event {
    private final List<Cond> mConditions = new ArrayList<>();
    private final List<Action> mActions = new ArrayList<>();
    private final Logger mLogger;
    private final String mSrcLine;
    private boolean mExecuted;

    public Event(Logger logger, String srcLine) {
        mLogger = logger;
        mSrcLine = srcLine;
    }

    public void addConditional(IConditional condition, boolean negated) {
        mConditions.add(new Cond(condition, negated));
    }

    public void addAction(IIntFunction function, IIntValue value) {
        mActions.add(new Action(function, value));
    }

    boolean evalConditions() {
        if (mConditions.isEmpty()) {
            return false;
        }

        for (Cond condition : mConditions) {
            if (!condition.eval()) {
                return false;
            }
        }

        return true;
    }

    void execute() {
        if (!mExecuted) {
            for (Action action : mActions) {
                try {
                    mLogger.log("[Conductor] Exec: " + mSrcLine);
                    action.execute();

                } catch (Exception e) {
                    mLogger.log("[Conductor] Action failed [" + action + "]: " + e);
                }
            }

            mExecuted = true;
        }
    }

    boolean isExecuted() {
        return mExecuted;
    }

    void resetExecuted() {
        mExecuted = false;
    }
}
