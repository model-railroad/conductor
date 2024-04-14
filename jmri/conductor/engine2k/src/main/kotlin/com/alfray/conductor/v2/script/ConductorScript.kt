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

package com.alfray.conductor.v2.script

import com.alfray.conductor.v2.script.dsl.IConductor
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.compilerOptions
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm

/**
 * References:
 * https://www.youtube.com/watch?v=OEFwnWxoazI&t=1248s
 * https://kotlinlang.org/docs/custom-script-deps-tutorial.html
 *
 * For the IDE to take into account any changes here, a restart is needed.
 */
@KotlinScript(
    fileExtension = "conductor.kts",
    compilationConfiguration = ConductorScriptConfiguration::class
)
abstract class ConductorScript(conductor: IConductor) : IConductor by conductor

object ConductorScriptConfiguration : ScriptCompilationConfiguration({
    jvm {
        dependenciesFromClassContext(
            ConductorScript::class,
            // add libraries here
            "kotlin-stdlib",
            "kotlin-scripting-dependencies",
            "kotlin-reflect",
            "engine2k",
        )
    }
    compilerOptions.append("-Xadd-modules=ALL-MODULE-PATH")
    // defaultImports {}
})

