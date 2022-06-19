/*
 * Project: Conductor
 * Copyright (C) 2018 alf.labs gmail com,
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
package com.alfray.conductor.v2.script

import com.alflabs.kv.IKeyValue
import com.alflabs.manifest.Constants
import com.alfray.conductor.v2.dagger.Script2kScope
import javax.inject.Inject

/**
 * Handles the current EStop State shared with RTAC.
 * This is read by the [ExecEngine2k].
 * It can be set via the Script "ESTOP" command.
 *
 *
 * When activated, nothing happens here except setting the proper KV value.
 * The engine will read the new state at the next runtime exec loop and actually send
 * an EStop via the JMRI interface to all defined throttles. RTAC provides a way to
 * reset the state, which is also handled by the [ExecEngine2k] runtime loop.
 */
@Script2kScope
class EStopHandler @Inject constructor(
    private val keyValue: IKeyValue
) {
    private companion object {
        val TAG = EStopHandler::class.simpleName
    }

    var lastEStopState: Constants.EStopState = Constants.EStopState.NORMAL

    /**
     * Returns true if The EStop-State is defined and Normal.
     *
     * For a more predictable behavior, the absence of the EStop-State is treated as
     * a active case. This is one of these "should not happen" scenarios.
     */
    val eStopState: Constants.EStopState
        get() {
            val value = keyValue.getValue(Constants.EStopKey)
            if (value == null) return Constants.EStopState.ACTIVE
            try {
                return Constants.EStopState.valueOf(value)
            } catch (ignore: IllegalArgumentException) {
            }
            return Constants.EStopState.ACTIVE
        }

    fun activateEStop() {
        val value = keyValue.getValue(Constants.EStopKey)
        if (Constants.EStopState.ACTIVE.toString() != value) {
            keyValue.putValue(
                Constants.EStopKey,
                Constants.EStopState.ACTIVE.toString(),
                true /* broadcast */
            )
        }
    }

    fun reset() {
        lastEStopState = Constants.EStopState.NORMAL
        keyValue.putValue(Constants.EStopKey, lastEStopState.toString(), true /* broadcast */)
    }
}
