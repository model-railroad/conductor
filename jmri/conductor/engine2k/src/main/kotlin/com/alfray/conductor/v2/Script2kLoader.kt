package com.alfray.conductor.v2

import com.alfray.conductor.v2.dagger.Script2kScope
import com.alfray.conductor.v2.host.ConductorScriptHost
import com.alfray.conductor.v2.script.ConductorImpl
import com.alfray.conductor.v2.script.ExecEngine
import com.alfray.conductor.v2.script.impl.IExecEngine
import com.google.common.io.Resources
import java.util.*
import javax.inject.Inject
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.host.UrlScriptSource

@Script2kScope
internal class Script2kLoader @Inject constructor() {
    internal lateinit var result: ResultWithDiagnostics<EvaluationResult>
    @Inject internal lateinit var scriptHost: ConductorScriptHost
    @Inject lateinit var conductorImpl: ConductorImpl
        internal set
    @Inject lateinit var execEngine: ExecEngine
        internal set

    @Suppress("UnstableApiUsage")
    fun loadScriptFromFile(scriptName: String) {
        val extension = if (!scriptName.endsWith(".conductor.kts")) ".conductor.kts" else ""
        val scriptPath = "v2/script/$scriptName$extension"
        val scriptUrl = Resources.getResource(scriptPath)!!
        val source = UrlScriptSource(scriptUrl)
        result = loadScript(source)
    }

    fun loadScriptFromText(scriptName: String = "local", scriptText: String) {
        val source = StringScriptSource(scriptText, scriptName)
        result = loadScript(source)
    }

    private fun loadScript(source: SourceCode): ResultWithDiagnostics<EvaluationResult> {
        return scriptHost.eval(source, conductorImpl)
    }

    fun getResultOutputs() : String =
        result.reports.joinToString("\n") { it.toString() }

    fun getResultErrors() : List<String> =
        result.reports
            .filter {
                it.severity == ScriptDiagnostic.Severity.ERROR ||
                it.severity == ScriptDiagnostic.Severity.FATAL
            }.map { it.toString() }
}
