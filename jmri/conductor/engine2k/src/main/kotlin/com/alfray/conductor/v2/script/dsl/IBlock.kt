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

/**
 * DSL script interface for a JMRI-backed block.
 *
 * A "block state" is either empty, occupied, or trailing.
 *
 * The "active" property is cached and updated from the underlying JMRI sensor only
 * once per engine loop update for consistency. Script users should not rely on the "active"
 * property, and focus on the "state" property instead.
 */
interface IBlock : IActive, IVarName {
    /** The JMRI system name of the sensor detecting this block's track occupation. */
    val systemName: String

    /**
     * Provides a script-defined name for this sensor that differs from the JMRI system name.
     * Can only be set once.
     */
    infix fun named(name: String) : IBlock

    /** Occupancy state of the block. */
    enum class State {
        /** Block is empty. Underlying sensor should be inactive. */
        EMPTY,

        /** Block is known to be occupied. Underlying sensor is typically active,
         * yet may temporarily register as inactive if flaky.  */
        OCCUPIED,

        /** Block is trailing. It was occupied and train has moved on to the next block.
         * Underlying sensor is typically inactive, yet may temporary still be active
         * during block movement transition. */
        TRAILING
    }

    /** Occupancy state of the block: empty, occupied, or trailing. */
    val state: State
}
