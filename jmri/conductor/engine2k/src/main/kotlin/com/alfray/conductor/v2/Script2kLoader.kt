package com.alfray.conductor.v2

import com.alfray.conductor.v2.dagger.Script2kScope
import com.alfray.conductor.v2.host.ConductorScriptHost
import com.alfray.conductor.v2.script.ConductorImpl
import com.alfray.conductor.v2.script.ExecEngine2k
import com.google.common.io.Resources
import javax.inject.Inject
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.host.UrlScriptSource

@Script2kScope
class Script2kLoader @Inject constructor() {
    internal lateinit var result: ResultWithDiagnostics<EvaluationResult>
    @Inject internal lateinit var scriptHost: ConductorScriptHost
        internal set
    @Inject lateinit var conductorImpl: ConductorImpl
        internal set
    @Inject lateinit var execEngine: ExecEngine2k
        internal set
    private var errors: List<String> = emptyList()
    var status = Status.NotLoaded
        internal set

    @Suppress("UnstableApiUsage")
    fun loadScriptFromFile(scriptName: String) {
        status = Status.Loading
        val extension = if (!scriptName.endsWith(".conductor.kts")) ".conductor.kts" else ""
        val scriptPath = "v2/script/$scriptName$extension"
        val scriptUrl = Resources.getResource(scriptPath)!!
        val source = UrlScriptSource(scriptUrl)
        result = loadScript(source)
        errors = parseErrors(result)
        status = Status.Loaded
    }

    fun loadScriptFromText(scriptName: String = "local", scriptText: String) {
        status = Status.Loading
        val source = StringScriptSource(scriptText, scriptName)
        result = loadScript(source)
        errors = parseErrors(result)
        status = Status.Loaded
    }

    private fun loadScript(source: SourceCode): ResultWithDiagnostics<EvaluationResult> {
        return scriptHost.eval(source, conductorImpl)
    }

    fun getResultOutputs() : String =
        result.reports.joinToString("\n") { it.toString() }

    fun getResultErrors() = errors

    private fun parseErrors(results: ResultWithDiagnostics<EvaluationResult>) : List<String> {
        return results.reports
            .filter {
                it.severity == ScriptDiagnostic.Severity.ERROR ||
                it.severity == ScriptDiagnostic.Severity.FATAL
            }.map { it.toString() }
    }

    enum class Status {
        NotLoaded,
        Loading,
        Loaded
    }
}
