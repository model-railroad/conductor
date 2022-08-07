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

/** DSL script interface for a JMRI sensor. */
interface ISensor : IActive, IVarName {
    /**
     * Changes the internal state of the sensor, and propagates that state to the JMRI sensor.
     * This only affects the "virtual" sensor (e.g. the internal state for JMRI or Conductor)
     * and does not affect the real backing sensor (e.g. NCE AIU card, etc.)
     */
    fun active(isActive: Boolean)

    /** The JMRI system name of the sensor. */
    val systemName: String

    /**
     * Provides a script-defined name for this sensor that differs from the JMRI system name.
     * Can only be set once.
     */
    infix fun named(name: String) : ISensor
}
