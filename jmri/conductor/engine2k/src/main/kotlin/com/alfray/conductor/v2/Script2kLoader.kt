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
import com.alfray.conductor.v2.script.CurrentContext
import com.alfray.conductor.v2.script.ExecEngine2k
import com.alfray.conductor.v2.utils.ConductorExecException
import com.google.common.io.Resources
import java.io.File
import java.util.jar.Attributes
import java.util.jar.JarFile
import javax.inject.Inject
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.api.onFailure
import kotlin.script.experimental.api.valueOrNull
import kotlin.script.experimental.host.FileScriptSource
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
    private val TAG = javaClass.simpleName
    @Inject internal lateinit var currentContext: CurrentContext
    @Inject internal lateinit var scriptHost: ConductorScriptHost
    internal lateinit var result: ResultWithDiagnostics<EvaluationResult>
    var status = Status.NotLoaded
        internal set

    @Suppress("UnstableApiUsage")
    fun loadScriptFromFile(scriptName: String) {
        status = Status.Loading

        updateScriptClasspath()

        logger.d(TAG, "Java   Runtime Version: " + System.getProperty("java.version"))
        logger.d(TAG, "Kotlin Runtime Version: " + KotlinVersion.CURRENT)
        logger.d(TAG, "Kotlin Script Classpath: " + System.getProperty("kotlin.script.classpath"))

        // Try the argument as a file path.
        var scriptFile = File(scriptName)
        var isFile = scriptFile.exists()
        if (!isFile) {
            // Maybe it lacks the extension?
            if (!scriptName.endsWith(".conductor.kts")) {
                scriptFile = File("$scriptName.conductor.kts")
                isFile = scriptFile.exists()
            }
        }

        val source: SourceCode
        if (isFile) {
            // Load the file we just found as-is.
            source = FileScriptSource(scriptFile)
        } else {
            // Try to load it as a resource
            val extension = if (!scriptName.endsWith(".conductor.kts")) ".conductor.kts" else ""
            // Note: JAR resource paths always use /, not File.separator.
            val scriptPath = "v2/script/$scriptName$extension"
            scriptFile = File(scriptPath)
            val scriptUrl = Resources.getResource(scriptPath)!!
            logger.d(TAG, "Loading script from ${scriptUrl.path}")
            source = UrlScriptSource(scriptUrl)
        }

        scriptSource.scriptInfo = Script2kSourceInfo(source.name ?: scriptName, scriptFile, source)
        result = loadScript(source)
        status = Status.Loaded
    }

    private fun updateScriptClasspath() {
        // Kotlin Scripting ClassPath is missing when executed from a FatJAR.
        // https://youtrack.jetbrains.com/issue/KT-21443
        // Get the JAR path and add it to kotlin.script.classpath, along with
        // any JAR Manifest Class-Path.
        val path = this::class.java.protectionDomain.codeSource.location.path
        logger.d(TAG, "JAR Path = $path")
        try {
            val jar = JarFile(path)
            val manifest = jar.manifest
            val mainAttr = manifest.mainAttributes
            val classPaths = mutableListOf<String>()
            if (mainAttr.containsKey(Attributes.Name.CLASS_PATH)) {
                val manifestCP = mainAttr[Attributes.Name.CLASS_PATH] as String
                val sepCP = System.getProperty("path.separator")
                classPaths.add(manifestCP.replace(" ", sepCP))
            }
            classPaths.add(path)
            System.setProperty("kotlin.script.classpath", classPaths.joinToString(":"))
        } catch (e: Exception) {
            logger.d(TAG, "Failed to parse JAR ClassPath", e)
        }
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
        currentContext.changeContext(currentContext.scriptLoaderContext)
        try {
            return scriptHost.eval(source, conductorImpl).onFailure {
                scriptErrors.errors.addAll(parseErrors(it))
            }
        } finally {
            currentContext.resetContext()
        }
    }

    fun getResultOutputs() : String =
        result.reports.joinToString("\n") { it.toString() }

    fun getResultErrors() = scriptErrors.errors.toList()

    private fun parseErrors(results: ResultWithDiagnostics<EvaluationResult>) : List<String> {
        val errors = mutableListOf<String>()

        val value = results.valueOrNull()
        value?.let {
            if (it.returnValue is ResultValue.Error) {
                val rvErr = it.returnValue as ResultValue.Error
                val t = rvErr.error
                if (t is ConductorExecException && t.message != null) {
                    errors.add(t.message!!)
                } else {
                    errors.add(t.toString())
                }
            }
        }

        results.reports
            .filter {
                it.severity == ScriptDiagnostic.Severity.ERROR ||
                it.severity == ScriptDiagnostic.Severity.FATAL
            }.mapTo(errors) {
                it.render(
                    withSeverity = true,
                    withLocation = true,
                    withException = true,
                    withStackTrace = false,)
            }

        return errors
    }

    fun addError(t: Throwable) {
        when (t) {
            is ConductorExecException -> scriptErrors.add(t.message ?: t.toString())
            else -> scriptErrors.add(t.toString())
        }
    }

    enum class Status {
        NotLoaded,
        Loading,
        Loaded
    }
}
