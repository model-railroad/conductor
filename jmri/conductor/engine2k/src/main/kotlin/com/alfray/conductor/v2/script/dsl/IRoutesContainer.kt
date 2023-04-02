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

/** DSL script interface for a list route, of which only one can be active at a time. */
interface IRoutesContainer {
    /** The exported name of this route. Displayed on RTAC. */
    val name: String

    /** The toggle sensor displayed on RTAC. */
    val toggle: ISensor

    /** The exported status of this route. Displayed on RTAC. Defaults to 'Idle'. */
    val status: () -> String

    /** The currently active route. */
    val active : IRoute

    /** All the route choices for this routes' container. */
    val routes : List<IRoute>

    /** Activates this route, or resets & re-activates the current route. */
    fun activate(route: IRoute)

    /** True if the current route is in error. Useful as a script on..then condition test. */
    val error : Boolean

    /** Registers a new idle route. */
    fun idle(idleRouteSpecification: IIdleRouteBuilder.() -> Unit): IRoute

    /** Registers a new sequence route. */
    fun sequence(sequenceRouteSpecification: ISequenceRouteBuilder.() -> Unit): IRoute

    /** Returns a string representation of the status of that route. */
    fun getLogStatus(): String
}

