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

/** DSL script interface for an SVG Map definition. */
interface ISvgMap {
    val name: String
    val svg: String
    val displayOn: SvgMapTarget
}

/** DSL script interface to build an [ISvgMap]. */
interface ISvgMapBuilder {
    var name: String
    var svg: String
    var displayOn: SvgMapTarget
}

enum class SvgMapTarget {
    Conductor,
    RTAC,
}
