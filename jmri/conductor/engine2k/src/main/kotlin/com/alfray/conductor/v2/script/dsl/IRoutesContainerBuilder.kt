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

/** Interal DSL script interface to create a new [IRoutesContainer]. */
interface IRoutesContainerBuilder {
    /**
     * The exported name of this route. Mandatory.
     * This name is displayed on the RTAC tablet display.
     */
    var name: String

    /**
     * The toggle sensor that triggers route enablement. Mandatory.
     * The toggle value has no bearing on the actual script execution -- it is up to the
     * script to test for the toggle when and where needed.
     * This is only used to display the toggle state on the RTAC tablet display.
     */
    var toggle: ISensor

    /**
     * The exported status of this route.
     * This state is displayed on the RTAC tablet display.
     * This is a callable (a.k.a. block in kotlin) that returns a string, queried lazily.
     * The default implementation returns the string "Idle".
     */
    var status: () -> String

    /** Callback when a route becomes in error. */
    fun onError(action: TAction)
}
