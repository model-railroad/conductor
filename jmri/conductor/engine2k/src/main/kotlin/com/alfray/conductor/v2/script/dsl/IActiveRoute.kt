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

package com.alfray.conductor.v2.script.dsl

interface IActiveRoute {
    /** The currently active route. */
    val active : IRoute

    /** All the route choices for this active route. */
    val routes : List<IRoute>

    /** Activates this route. */
    fun activate(route: IRoute)

    /** The current route and this are in error. */
    val error : Boolean

    /** Registers a new idle route. */
    fun idle(): IRoute

    /** Registers a new sequence route. */
    fun sequence(init: IRouteSequenceBuilder.() -> Unit): IRoute
}
