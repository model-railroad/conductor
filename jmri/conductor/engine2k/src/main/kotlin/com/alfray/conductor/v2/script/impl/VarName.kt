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

import com.alfray.conductor.v2.script.dsl.IVarName

internal abstract class VarName : IVarName {
    private var _name = ""

    /** Returns the internal name or the default name. */
    override val name: String
        get() = _name.ifEmpty { this.defaultName() }

    /** Sets the internal variable name. Can be set only once. Ignored if set to the same name. */
    fun setNamed(name: String) {
        check(_name.isEmpty() || _name == name) {
            "Variable name already set to '$_name', cannot be changed to '$name'."
        }
        _name = name
    }

    /** Provides the default var name if not customized by the script. */
    abstract fun defaultName(): String
}
