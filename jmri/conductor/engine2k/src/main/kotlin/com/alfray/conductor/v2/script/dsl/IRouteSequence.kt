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

import com.alfray.conductor.v2.simulator.SimulRouteGraph

/** DSL script interface for a route sequence. */
interface IRouteSequence : IRoute {
    /** The [IThrottle] operating on this route. */
    val throttle: IThrottle

    /** Internal Converts the route graph into a Simulator route graph. */
    fun toSimulGraph(): SimulRouteGraph
    // TODO IRouteSequence.toSimulGraph should not be exposed in the DSL.
}
