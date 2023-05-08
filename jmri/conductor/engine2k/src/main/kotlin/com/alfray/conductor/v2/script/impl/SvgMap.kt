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

import com.alflabs.manifest.MapInfo
import com.alflabs.utils.FileOps
import com.alfray.conductor.v2.script.dsl.ISvgMap
import com.alfray.conductor.v2.script.dsl.ISvgMapBuilder
import com.alfray.conductor.v2.script.dsl.SvgMapTarget
import com.google.common.base.Charsets
import com.google.common.io.Resources
import java.io.File
import java.io.IOException

internal class SvgMapBuilder() : ISvgMapBuilder {
    override lateinit var name: String
    override lateinit var svg: String
    override lateinit var displayOn: SvgMapTarget

    constructor(name: String, svg: String, displayOn: SvgMapTarget) : this() {
        this.name = name
        this.svg = svg
        this.displayOn = displayOn
    }

    fun create() : ISvgMap = SvgMap(this)
}

class SvgMap(builder: ISvgMapBuilder) : ISvgMap {
    override val name = builder.name
    override val svg = builder.svg
    override val displayOn = builder.displayOn

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SvgMap

        if (name != other.name) return false
        if (svg != other.svg) return false
        if (displayOn != other.displayOn) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + svg.hashCode()
        result = 31 * result + displayOn.hashCode()
        return result
    }

    /**
     * Reads the SVG Map and returns a [MapInfo] with both the filename and the SVG data.
     * <p/>
     * The "svg path" can either be a file (relative to the optional script directory)
     * or a JAR resource path.
     * <p/>
     * The [displayOn] field is not exported to [MapInfo] since only RTAC maps need to be exported.
     *
     * @throws IOException if can't read the map.
     */
    @Suppress("UnstableApiUsage")
    @Throws(IOException::class)
    fun toMapInfo(fileOps: FileOps, scriptDir: File?): MapInfo {
        var svgPath = svg
        val svgFile = if (scriptDir == null) File(svgPath) else File(scriptDir, svgPath)
        var svgData: String

        try {
            // Try loading from the file path
            svgData = fileOps.toString(svgFile, Charsets.UTF_8)
            svgPath = svgFile.path
        } catch (e1: IOException) {
            try {
                // Otherwise, try loading it as a resource path.
                // Note: JAR resource paths always use /, not File.separator.
                val url = Resources.getResource(svgFile.path.replace(File.separatorChar, '/'))
                svgData = Resources.toString(url, Charsets.UTF_8)
                svgPath = url.path
            } catch (e2: Exception) {
                throw e1
            }
        }

        return MapInfo(name, svgData, svgPath)
    }
}
