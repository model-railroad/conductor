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

/** An abstract internal DSL script interface common to route builders. */
interface IRouteBaseBuilder {
    /** The name of this route in its container. Optional yet recommended.
     * This is mostly useful for debugging and informational purposes, to distinguish different routes. */
    var name: String

    /** Callback when a route is being activated. Called once after activation. */
    fun onActivate(action: TAction)

    /** Callback called once when a route becomes in error. */
    fun onError(action: TAction)
}

