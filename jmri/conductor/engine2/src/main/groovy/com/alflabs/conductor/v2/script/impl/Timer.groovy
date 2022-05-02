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

class Timer extends BaseActive {
    private final Optional<Timer> mBaseTimer
    private final int mDelay
    private boolean mIsStarted

    Timer(int delay) {
        mDelay = delay
        mBaseTimer = Optional.empty()
    }

    Timer(Timer baseTimer, int delay) {
        mDelay = delay
        mBaseTimer = Optional.of(baseTimer)
    }

    int getDelay() {
        return mDelay + (mBaseTimer.isPresent() ? mBaseTimer.get().getDelay() : 0)
    }

    void start() {
        mIsStarted = true
    }

    void reset() {
        mIsStarted = false
    }

    boolean isStarted() {
        return mIsStarted
    }
}
