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

class SequenceNodeEvents {
    private Optional<IEvalRule> mOnEnterRule = Optional.empty()
    private Optional<IEvalRule> mWhileOccupiedRule = Optional.empty()
    private Optional<IEvalRule> mOnTrailingRule = Optional.empty()
    private Optional<IEvalRule> mOnEmptyRule = Optional.empty()

    void onEnter(@NonNull @DelegatesTo(RootScript) Closure action) {
        mOnEnterRule = Optional.of(new RuleAlways(action))
    }

    @NonNull
    Optional<IEvalRule> getOnEnterRule() {
        return mOnEnterRule
    }

    void whileOccupied(@NonNull @DelegatesTo(RootScript) Closure action) {
        mWhileOccupiedRule = Optional.of(new RuleAlways(action))
    }

    @NonNull
    Optional<IEvalRule> getWhileOccupiedRule() {
        return mWhileOccupiedRule
    }

    void onTrailing(@NonNull @DelegatesTo(RootScript) Closure action) {
        mOnTrailingRule = Optional.of(new RuleAlways(action))
    }

    @NonNull
    Optional<IEvalRule> getOnTrailingRule() {
        return mOnTrailingRule
    }

    void onEmpty(@NonNull @DelegatesTo(RootScript) Closure action) {
        mOnEmptyRule = Optional.of(new RuleAlways(action))
    }

    @NonNull
    Optional<IEvalRule> getOnEmptyRule() {
        return mOnEmptyRule
    }
}
