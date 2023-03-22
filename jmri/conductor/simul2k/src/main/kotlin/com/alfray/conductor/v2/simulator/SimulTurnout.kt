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

import com.alflabs.conductor.jmri.IJmriTurnout
import com.alflabs.utils.ILogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

/** Creates a new turnout for the given JMRI system name. */
@AssistedFactory
interface ISimulTurnoutFactory {
    fun create(systemName: String) : SimulTurnout
}

/**
 * Simulated turnout. This simply mirrors the programmatic state set using [setTurnout].
 */
class SimulTurnout @AssistedInject constructor(
    private val logger: ILogger,
    @Assisted val systemName: String
) : IJmriTurnout {
    private val TAG = javaClass.simpleName
    private var _normal = true

    override fun isNormal(): Boolean {
        return _normal
    }

    override fun setTurnout(normal: Boolean) {
        _normal = normal
        logger.d(TAG, String.format("[%s] Turnout: %s", systemName, if (normal) "Normal" else "Reverse"))
    }
}
