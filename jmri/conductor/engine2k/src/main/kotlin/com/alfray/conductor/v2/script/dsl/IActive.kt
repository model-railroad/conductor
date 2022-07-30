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

/** Base abstract DSL script interface for any object with a dynamic [active] state. */
interface IActive {
    /**
     * Object is active.
     * This is a read-only property that reflects the state of this object.
     * What makes an object "active" depends on each object, for example a track sensor sensing
     * block occupation, or a turnout in normal state.
     * For most JMRI-based objects, the property is cached during script execution in order
     * for all rules to have a consistent view of the inputs.
     */
    val active: Boolean

    /** Object is not active. */
    operator fun not() : Boolean
}
