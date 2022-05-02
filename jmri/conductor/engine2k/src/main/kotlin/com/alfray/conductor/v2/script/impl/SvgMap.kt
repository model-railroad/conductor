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

import com.alfray.conductor.v2.script.dsl.ISvgMap
import com.alfray.conductor.v2.script.dsl.ISvgMapBuilder

internal class SvgMapBuilder constructor() : ISvgMapBuilder {
    override lateinit var name: String
    override lateinit var svg: String

    constructor(name: String, svg: String) : this() {
        this.name = name
        this.svg = svg
    }

    fun create() : ISvgMap = SvgMap(this)
}

internal class SvgMap(builder: ISvgMapBuilder) : ISvgMap {
    override val name = builder.name
    override val svg = builder.svg

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SvgMap

        if (name != other.name) return false
        if (svg != other.svg) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + svg.hashCode()
        return result
    }
}
