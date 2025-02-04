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
import com.alfray.conductor.v2.script.dsl.IRoutesContainer
import com.alfray.conductor.v2.script.dsl.IRouteBaseBuilder
import com.alfray.conductor.v2.script.dsl.TAction
import com.alfray.conductor.v2.utils.assertOrThrow

internal open class RouteBaseBuilder(
    protected val owner: IRoutesContainer,
    protected val logger: ILogger,
) : IRouteBaseBuilder {
    private val TAG = javaClass.simpleName
    var actionOnActivate: TAction? = null
    var actionOnError: TAction? = null
    override var name: String = ""
    override val routes: IRoutesContainer
        get() = owner

    override fun onActivate(action: TAction) {
        logger.assertOrThrow(TAG, actionOnActivate == null) {
            "Route onActive defined more than once"
        }
        actionOnActivate = action
    }

    override fun onError(action: TAction) {
        logger.assertOrThrow(TAG, actionOnError == null) {
            "Route onError defined more than once"
        }
        actionOnError = action
    }
}
