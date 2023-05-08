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
import com.alfray.conductor.v2.script.dsl.IRoute
import com.alfray.conductor.v2.script.dsl.IIdleRouteBuilder
import com.alfray.conductor.v2.script.dsl.TAction
import com.alfray.conductor.v2.utils.assertOrThrow
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

internal open class IdleRouteBuilder @AssistedInject constructor(
        logger: ILogger,
        private val factory: Factory,
        @Assisted owner: IRoutesContainer,
) : RouteBaseBuilder(owner as RoutesContainer, logger), IIdleRouteBuilder {
    private val TAG = javaClass.simpleName
    var actionOnIdle: TAction? = null

    override fun onIdle(action: TAction) {
        logger.assertOrThrow(TAG, actionOnIdle == null) {
            "Route onIdle defined more than once"
        }
        actionOnIdle = action
    }

    fun create() : IRoute = factory.createIdleRoute(owner, this)
}
