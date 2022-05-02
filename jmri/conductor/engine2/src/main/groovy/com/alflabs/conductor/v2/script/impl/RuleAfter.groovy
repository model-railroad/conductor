/*
 * Project: Conductor
 * Copyright (C) 2022 alf.labs gmail com,
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

package com.alflabs.conductor.v2.script.impl

import com.alflabs.annotations.NonNull
import com.alflabs.conductor.v2.script.RootScript

/** A after...then...then_after rule. */
class RuleAfter implements IEvalRule {
    private final Timer mAfterTimer
    private final AndAfterContinuation mAndAfterContinuation
    private Closure mAction

    interface AndAfterContinuation {
        abstract RuleAfter and_after(Timer newTimer)
    }

    RuleAfter(@NonNull Timer afterTimer, @NonNull AndAfterContinuation andAfterContinuation) {
        this.mAfterTimer = afterTimer
        this.mAndAfterContinuation = andAfterContinuation
    }

    AndAfterContinuation then(@NonNull @DelegatesTo(RootScript) Closure action) {
        mAction = action
        return mAndAfterContinuation
    }

    @Override
    boolean evaluateCondition() {
        return mAfterTimer.isActive()
    }

    @Override
    void evaluateAction(@NonNull RootScript rootScript) {
        def code = mAction.rehydrate(rootScript /*delegate*/, this /*owner*/, this /*this*/)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code.call()
    }
}
