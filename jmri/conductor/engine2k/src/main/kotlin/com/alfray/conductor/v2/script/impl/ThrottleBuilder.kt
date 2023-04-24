/*
 * Project: Conductor
 * Copyright (C) 2023 alf.labs gmail com,
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

import com.alflabs.utils.ILogger
import com.alfray.conductor.v2.script.dsl.IThrottle
import com.alfray.conductor.v2.script.dsl.IThrottleBuilder
import com.alfray.conductor.v2.script.dsl.TBooleanAction
import com.alfray.conductor.v2.utils.assertOrThrow

internal class ThrottleBuilder(
    private val logger: ILogger
) : IThrottleBuilder {
    private val TAG = javaClass.simpleName
    override lateinit var throttle: IThrottle
    var actionOnLight: TBooleanAction? = null
    var actionOnSound: TBooleanAction? = null
    var actionOnBell: TBooleanAction? = null

    /** Callback implementing [IThrottle.light]. The default implementation toggles F0. */
    override fun onLight(action: TBooleanAction) {
        logger.assertOrThrow(TAG, actionOnLight == null) {
            "Throttle onLight defined more than once"
        }
        actionOnLight = action
    }

    /** Callback implementing [IThrottle.sound]. The default implementation toggles F8. */
    override fun onSound(action: TBooleanAction) {
        logger.assertOrThrow(TAG, actionOnSound == null) {
            "Throttle onSound defined more than once"
        }
        actionOnSound = action
    }

    /** Callback implementing [IThrottle.bell]. The default implementation toggles F1. */
    override fun onBell(action: TBooleanAction) {
        logger.assertOrThrow(TAG, actionOnBell == null) {
            "Throttle onBell defined more than once"
        }
        actionOnBell = action
    }
}
