/*
 * Project: Conductor
 * Copyright (C) 2017 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alflabs.conductor.script;

import com.alflabs.conductor.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents one event with its condition list, its action list and an "execution engine".
 */
public class Event {
    private final List<Cond> mConditions = new ArrayList<>();
    private final List<IAction> mActions = new ArrayList<>();
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

    public void addIntAction(IIntFunction function, IIntValue value) {
        mActions.add(new IntAction(function, value));
    }

    public void addStringAction(IStringFunction function, IStringValue value) {
        mActions.add(new StringAction(function, value));
    }

    boolean evalConditions(CondCache condCache) {
        if (mConditions.isEmpty()) {
            return false;
        }

        for (Cond condition : mConditions) {
            if (!condition.eval(condCache)) {
                return false;
            }
        }

        return true;
    }

    void execute() {
        if (!mExecuted) {
            for (IAction action : mActions) {
                try {
                    mLogger.log("[Conductor] Exec: " + mSrcLine);
                    action.execute();

                } catch (Exception e) {
                    mLogger.log("[Conductor] IAction failed [" + action + "]: " + e);
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
