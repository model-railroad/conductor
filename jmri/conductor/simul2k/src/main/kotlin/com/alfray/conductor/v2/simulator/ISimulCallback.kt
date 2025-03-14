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

package com.alfray.conductor.v2.simulator

/** Callback provided to the ExecEngine to update the simulator based on actual route state. */
interface ISimulCallback {
    /** Notifies the simulator that the sum of timers for that block have changed. */
    fun onBlockTimersChanged(systemName: String, sumTimersSec: Int)

    /** Sets the route definition for a given DCC throttle. */
    fun setRoute(dccAddress: Int, minSecondsOnBlock: Int, maxSecondsOnBlock: Int, graph: SimulRouteGraph)
}
