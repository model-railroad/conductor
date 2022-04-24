package com.alfray.conductor.v2

import com.alfray.conductor.v2.host.ConductorScriptHost
import com.alfray.conductor.v2.script.ConductorImpl
import com.alfray.conductor.v2.script.ExecEngine
import com.alfray.conductor.v2.script.IConductor
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.host.UrlScriptSource
import com.google.common.io.Resources
import java.util.*

class Script2kLoader {
    private lateinit var scriptHost: ConductorScriptHost
    lateinit var conductorImpl: ConductorImpl
    lateinit var execEngine: ExecEngine
    lateinit var result: ResultWithDiagnostics<EvaluationResult>

    fun execEngineOptional() : Optional<ExecEngine> =
        if (::execEngine.isInitialized) Optional.of(execEngine) else Optional.empty()

    fun conductorOptional() : Optional<ConductorImpl> =
        if (::conductorImpl.isInitialized) Optional.of(conductorImpl) else Optional.empty()

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
        conductorImpl = ConductorImpl()
        scriptHost = ConductorScriptHost()
        execEngine = ExecEngine(conductorImpl)
        return scriptHost.eval(source, conductorImpl)
    }

    fun getResultErrors() : List<String> =
        result.reports
            .filter { it.severity != ScriptDiagnostic.Severity.DEBUG }
            .map { it.toString() }
}
