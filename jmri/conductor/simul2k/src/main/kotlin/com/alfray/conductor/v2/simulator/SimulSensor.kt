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

package com.alfray.conductor.v2.simulator

import com.alflabs.conductor.jmri.IJmriSensor
import com.alflabs.utils.ILogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.random.Random

/**
 * Simulated sensor. This simply mirrors the programmatic state set using [setActive].
 */
class SimulSensor @AssistedInject constructor(
    private val logger: ILogger,
    @Assisted val systemName: String
) : IJmriSensor {
    private val TAG = javaClass.simpleName
    private var _active = false
    private var _randomize = 0.0

    fun setRandomize(randomThreshold: Double) {
        _randomize = randomThreshold
    }

    override fun isActive(): Boolean {
        var active = _active
        if (active && _randomize > 0 && Random.nextDouble() < _randomize) {
            logger.d(TAG, "Flaky sensor $systemName is off")
            active = false
        }
        return active
    }

    override fun setActive(active: Boolean) {
        if (!active) {
            _randomize = 0.0
        }
        _active = active
    }
}
