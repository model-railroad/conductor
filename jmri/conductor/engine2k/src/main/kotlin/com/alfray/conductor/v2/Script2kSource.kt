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

package com.alfray.conductor.v2

import com.alfray.conductor.v2.dagger.Script2kScope
import java.io.File
import javax.inject.Inject
import kotlin.script.experimental.api.SourceCode

/** Information on the last script loaded by [Script2kLoader], if any. */
@Script2kScope
class Script2kSource @Inject constructor() {
    var scriptInfo: Script2kSourceInfo? = null

    /** Accessor method that returns the scriptPath parent or null.
     * The script path is null for a string-based junit test script. */
    fun scriptPath(): File? = scriptInfo?.scriptPath

    /** Accessor method that returns the scriptPath parent or null.
     * The script parent dir is null for a string-based junit test script. */
    fun scriptDir(): File? = scriptInfo?.let { it.scriptPath?.parentFile }
}

data class Script2kSourceInfo(
    val scriptName: String,
    val scriptPath: File?,       // null for a string-based junit test script
    val scriptSource: SourceCode,
)
