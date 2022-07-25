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

import com.alflabs.utils.ILogger
import com.alfray.conductor.v2.dagger.Script2kScope
import com.alfray.conductor.v2.host.ConductorScriptHost
import com.alfray.conductor.v2.script.ConductorImpl
import com.alfray.conductor.v2.script.ExecEngine2k
import com.google.common.io.Resources
import java.io.File
import javax.inject.Inject
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.host.UrlScriptSource

@Script2kScope
class Script2kLoader @Inject constructor(
    val logger: ILogger,
    val conductorImpl: ConductorImpl,
    val execEngine: ExecEngine2k,
    val scriptErrors: Script2kErrors,
    val scriptSource: Script2kSource,
) {
    @Inject internal lateinit var scriptHost: ConductorScriptHost
    internal lateinit var result: ResultWithDiagnostics<EvaluationResult>
    var status = Status.NotLoaded
        internal set

    private companion object {
        val TAG = Script2kLoader::class.simpleName
    }

    @Suppress("UnstableApiUsage")
    fun loadScriptFromFile(scriptName: String) {
        status = Status.Loading
        val extension = if (!scriptName.endsWith(".conductor.kts")) ".conductor.kts" else ""
        // Note: JAR resource paths always use /, not File.separator.
        val scriptPath = "v2/script/$scriptName$extension"
        val scriptUrl = Resources.getResource(scriptPath)!!
        logger.d(TAG, "Loading script from ${scriptUrl.path}")
        val source = UrlScriptSource(scriptUrl)
        scriptSource.scriptInfo = Script2kSourceInfo(scriptName, File(scriptPath), source)
        result = loadScript(source)
        scriptErrors.errors.addAll(parseErrors(result))
        status = Status.Loaded
    }

    fun loadScriptFromText(scriptName: String = "local", scriptText: String) {
        status = Status.Loading
        val source = StringScriptSource(scriptText, scriptName)
        scriptSource.scriptInfo = Script2kSourceInfo(scriptName, null, source)
        result = loadScript(source)
        scriptErrors.errors.addAll(parseErrors(result))
        status = Status.Loaded
    }

    private fun loadScript(source: SourceCode): ResultWithDiagnostics<EvaluationResult> {
        return scriptHost.eval(source, conductorImpl)
    }

    fun getResultOutputs() : String =
        result.reports.joinToString("\n") { it.toString() }

    fun getResultErrors() = scriptErrors.errors.toList()

    private fun parseErrors(results: ResultWithDiagnostics<EvaluationResult>) : List<String> {
        return results.reports
            .filter {
                it.severity == ScriptDiagnostic.Severity.ERROR ||
                it.severity == ScriptDiagnostic.Severity.FATAL
            }.map {
                it.render(
                    withSeverity = true,
                    withLocation = true,
                    withException = true,
                    withStackTrace = false,)
            }
    }

    enum class Status {
        NotLoaded,
        Loading,
        Loaded
    }
}
