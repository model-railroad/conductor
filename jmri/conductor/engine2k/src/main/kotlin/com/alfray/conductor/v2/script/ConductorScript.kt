package com.alfray.conductor.v2.script

import com.alfray.conductor.v2.script.dsl.IConductor
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
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
    // defaultImports {}
})

