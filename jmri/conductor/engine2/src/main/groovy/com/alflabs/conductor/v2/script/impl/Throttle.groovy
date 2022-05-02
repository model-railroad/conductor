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

import com.alflabs.conductor.v2.script.impl.BaseVar

class Throttle extends BaseVar {
    private final int mDccAddress
    private boolean mIsStarted
    private int mSpeed = 0
    private boolean mLight
    private boolean mF1

    Throttle(int dccAddress) {
        mDccAddress = dccAddress
    }

    int getDccAddress() {
        return mDccAddress
    }

    int getSpeed() {
        return mSpeed
    }

    // forward as a function: { Throttle forward(speed) }
    void forward(int speed) {
        mSpeed = speed
    }

    // forward as a setter property: { Throttle.forward = speed }
    // Design-wise, Groovy makes it possible to have both the setter and the function to set
    // a value in the DSL, however it seems a tad confusing since no-argument methods must be
    // invoked as function (with parens). In the final DSL we'll disable the setter version.
    void setForward(int speed) {
        forward(speed)
    }

    // forward as a getter property: { Throttle.forward }
    boolean isForward() {
        return mSpeed > 0
    }

    void reverse(int speed) {
        mSpeed = -speed
    }

    boolean isReverse() {
        return mSpeed < 0
    }

    void stop() {
        mSpeed = 0
    }

    boolean isStopped() {
        return mSpeed == 0
    }

    void horn() {
        // no-op
    }

    void light(boolean lightOn) {
        mLight = lightOn
    }

    boolean isLight() {
        return mLight
    }

    void F1(boolean functionOn) {
        mF1 = functionOn
    }

    boolean isF1() {
        return mF1
    }

}
