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

@file:Suppress("FunctionName")

package com.alfray.conductor.v2.script.dsl

/** DSL script interface for a generic abstract route. */
interface IRoute {
    /** The [IActiveRoute] containing this route. */
    val owner: IActiveRoute

    /** Activates this route in its [IActiveRoute], or resets & re-activates the current route. */
    fun activate()

    /**
     * Changes the default starting block for this route.
     * Only effective if called from the onActivate callback.
     * Throws an error if the node is not part of the route.
     * Throws an error if called on a route without nodes such as an Idle Route.
     */
    fun start_node(node: INode)
}
