/*
 * Project: Conductor
 * Copyright (C) 2023 alf.labs gmail com,
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

/** Internal DSL script interface to provide callbacks for a new [IThrottle]. */
interface IThrottleBuilder {
    /** The throttle to use when implementation DCC functions changes. */
    val throttle: IThrottle

    /**
     * Provides a script-defined name for this throttle that differs from the JMRI system name.
     * Can only be set once.
     */
    var name : String?

    /** Callback implementing [IThrottle.light]. The default implementation toggles F0. */
    fun onLight(action: TBooleanAction)

    /** Callback implementing [IThrottle.sound]. The default implementation toggles F8. */
    fun onSound(action: TBooleanAction)

    /** Callback implementing [IThrottle.bell]. The default implementation toggles F1. */
    fun onBell(action: TBooleanAction)
}
