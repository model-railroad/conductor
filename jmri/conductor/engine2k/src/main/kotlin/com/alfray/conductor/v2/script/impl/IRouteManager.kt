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

/** Internal interface for a route that must be managed. */
interface IRouteManager {
    /**
     * Called from ExecEngine2's onExecStart to initialize and validate the state of the route.
     */
    fun initRouteManager()

    /**
     * Called at the start of each ExecEngine2 loop to manage the route.
     * This is called before the route gets to evaluate all the current
     * node rules.
     * This is the proper place to evaluate current blocks to update the
     * route's current node, move to the next node, update trailing ones,
     * enter error if an unknown block is activated, etc.
     */
    fun manageRoute()
}
