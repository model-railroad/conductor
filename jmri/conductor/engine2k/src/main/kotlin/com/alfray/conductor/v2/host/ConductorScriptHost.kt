package com.alfray.conductor.v2.host

import com.alfray.conductor.v2.dagger.Script2kScope
import com.alfray.conductor.v2.script.ConductorScript
import com.alfray.conductor.v2.script.dsl.IConductor
import javax.inject.Inject
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.api.constructorArgs
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

@Script2kScope
internal class ConductorScriptHost @Inject constructor() {
    private val scriptingHost = BasicJvmScriptingHost()

    fun eval(source: SourceCode, conductor: IConductor) : ResultWithDiagnostics<EvaluationResult> =
        scriptingHost.evalWithTemplate<ConductorScript>(
            source,
            evaluation = {
                constructorArgs(conductor)
            }
        )
}
