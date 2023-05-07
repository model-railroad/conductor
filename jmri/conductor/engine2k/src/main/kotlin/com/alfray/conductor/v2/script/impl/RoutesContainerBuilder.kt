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

import com.alflabs.conductor.util.EventLogger
import com.alflabs.kv.IKeyValue
import com.alflabs.utils.IClock
import com.alflabs.utils.ILogger
import com.alfray.conductor.v2.script.dsl.IRoutesContainer
import com.alfray.conductor.v2.script.dsl.IRoutesContainerBuilder
import com.alfray.conductor.v2.script.dsl.ISensor
import com.alfray.conductor.v2.script.dsl.TAction
import com.alfray.conductor.v2.simulator.ISimulCallback
import com.alfray.conductor.v2.utils.assertOrThrow

internal class RoutesContainerBuilder(
    private val clock: IClock,
    private val logger: ILogger,
    private val eventLogger: EventLogger,
) : IRoutesContainerBuilder {
    private val TAG = javaClass.simpleName
    var actionOnError: TAction? = null
    override lateinit var name: String
    override lateinit var toggle: ISensor
    override lateinit var status: () -> String

    override fun onError(action: TAction) {
        logger.assertOrThrow(TAG, actionOnError == null) {
            "RoutesContainer onError defined more than once"
        }
        actionOnError = action
    }

    fun create(keyValue: IKeyValue, simulCallback: ISimulCallback?): IRoutesContainer {
        logger.assertOrThrow(TAG, this::name.isInitialized) {
            "RoutesContainer 'name' property has not been defined."
        }
        logger.assertOrThrow(TAG, this::toggle.isInitialized) {
            "RoutesContainer $name 'toggle' property has not been defined."
        }
        if (!this::status.isInitialized) {
            // Sets the default for the active route.state to be "Idle".
            status = { "Idle" }
        }
        return RoutesContainer(clock, logger, keyValue, eventLogger, simulCallback, this)
    }
}
