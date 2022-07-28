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

import com.alflabs.utils.ILogger
import com.alfray.conductor.v2.script.dsl.IActiveRoute
import com.alfray.conductor.v2.script.dsl.IActiveRouteBuilder
import com.alfray.conductor.v2.script.dsl.ISensor
import com.alfray.conductor.v2.script.dsl.TAction
import com.alfray.conductor.v2.utils.assertOrThrow

internal class ActiveRouteBuilder(private val logger: ILogger) : IActiveRouteBuilder {
    private val TAG = javaClass.simpleName
    var actionOnError: TAction? = null
    override lateinit var name: String
    override lateinit var toggle: ISensor
    override lateinit var state: () -> String

    override fun onError(action: TAction) {
        logger.assertOrThrow(TAG, actionOnError == null) {
            "ActiveRoute onError defined more than once"
        }
        actionOnError = action
    }

    fun create(): IActiveRoute {
        logger.assertOrThrow(TAG, this::name.isInitialized) {
            "ActiveRoute 'name' property has not been defined."
        }
        logger.assertOrThrow(TAG, this::toggle.isInitialized) {
            "ActiveRoute $name 'toggle' property has not been defined."
        }
        if (!this::state.isInitialized) {
            // Sets the default for the active route.state to be "Idle".
            state = { "Idle" }
        }
        return ActiveRoute(logger, this)
    }
}
