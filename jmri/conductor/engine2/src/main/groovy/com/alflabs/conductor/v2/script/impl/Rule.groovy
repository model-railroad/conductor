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

/** A condition...then rule. */
class Rule implements IEvalRule {
    private final Closure<Boolean> mCondition
    private Closure mAction

    Rule(@NonNull Closure<Boolean> condition) {
        mCondition = condition
    }

    void then(@NonNull @DelegatesTo(RootScript) Closure action) {
        mAction = action
    }

    @Override
    boolean evaluateCondition() {
        return mCondition.call()
    }

    @Override
    void evaluateAction(@NonNull RootScript rootScript) {
        def code = mAction.rehydrate(rootScript /*delegate*/, this /*owner*/, this /*this*/)
        code.resolveStrategy = Closure.DELEGATE_FIRST
        code.call()
    }
}
