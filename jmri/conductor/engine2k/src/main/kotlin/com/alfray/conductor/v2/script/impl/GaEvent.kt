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

import com.alfray.conductor.v2.script.dsl.IGaEventBuilder

data class GaEvent(
    val category: String,
    val action: String,
    val label: String,
    val user: String,
)

internal class GaEventBuilder : IGaEventBuilder {
    override var category: String = ""
    override var action: String = ""
    override var label: String = ""
    override var user: String = ""

    fun create() : GaEvent = GaEvent(
        category, action, label, user
    )
}
