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

package com.alfray.conductor.v2.script.impl

import com.alflabs.conductor.jmri.IJmriProvider
import com.alflabs.conductor.jmri.IJmriThrottle
import com.alfray.conductor.v2.dagger.Script2kScope
import com.alfray.conductor.v2.script.dsl.DccSpeed
import com.alfray.conductor.v2.script.dsl.Delay
import com.alfray.conductor.v2.script.dsl.FBits
import com.alfray.conductor.v2.script.dsl.IThrottle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

@Script2kScope
@AssistedFactory
internal interface IThrottleFactory {
    fun create(dccAddress: Int) : Throttle
}

internal class Throttle @AssistedInject constructor(
    private val jmriProvider: IJmriProvider,
    @Assisted override val dccAddress: Int
) : VarName(), IThrottle, IExecEngine {
    private var _speed = DccSpeed(0)
    private var _light = false
    private var _sound = false
    private var _repeat = Delay(0)
    private var _f = FBits()
    private lateinit var jmriThrottle: IJmriThrottle

    override val speed: DccSpeed
        get() = _speed
    override val light: Boolean
        get() = _light
    override val sound: Boolean
        get() = _sound
    override val f: FBits
        get() = _f

    override fun named(name: String): IThrottle {
        setNamed(name)
        return this
    }

    override fun defaultName(): String = "Throttle-$dccAddress"

    override fun forward(speed: DccSpeed) {
        _speed = speed
    }

    override fun reverse(speed: DccSpeed) {
        _speed = speed.reverse()
    }

    override fun stop() {
        _speed = DccSpeed(0)
    }

    override fun horn() {
        // println("@@ Horn $dccAddress")
    }

    override fun light(on: Boolean) {
        _light = on
    }

    override fun sound(on: Boolean) {
        _sound = on
    }

    override fun repeat(repeat: Delay) {
        _repeat = repeat
    }

    override fun onExecStart() {
        jmriThrottle = checkNotNull(jmriProvider.getThrottle(dccAddress))
    }

    override fun onExecHandle() {
        TODO("Not yet implemented")
    }
}
