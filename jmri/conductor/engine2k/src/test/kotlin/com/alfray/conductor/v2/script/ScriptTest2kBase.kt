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

import com.alflabs.conductor.jmri.FakeJmriProvider
import com.alflabs.utils.FakeFileOps
import com.alflabs.utils.StringLogger
import com.alfray.conductor.v2.Script2kLoader
import com.alfray.conductor.v2.dagger.DaggerITestComponent2k
import com.alfray.conductor.v2.dagger.IScript2kTestComponent
import com.alfray.conductor.v2.dagger.ITestComponent2k
import com.alfray.conductor.v2.dagger.Script2kTestContext
import com.google.common.truth.Truth.assertThat
import javax.inject.Inject
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics

/** Base for script-based unit tests, loading script (from files or strings)
 * using the regular [Script2kLoader]. */
open class ScriptTest2kBase {
    protected val logger = StringLogger(/* useSysOut */ true)
    protected val jmriProvider = FakeJmriProvider(logger)
    protected lateinit var scriptComponent: IScript2kTestComponent
    @Inject internal lateinit var context: Script2kTestContext
    @Inject internal lateinit var fileOps: FakeFileOps

    internal lateinit var loader: Script2kLoader
    internal lateinit var conductorImpl: ConductorImpl
    internal lateinit var execEngine: ExecEngine2k

    fun createComponent(): ITestComponent2k {
        val mainComponent = DaggerITestComponent2k
            .factory()
            .createComponent(jmriProvider)
        mainComponent.inject(this)
        scriptComponent = context.createTestComponent()
        loader = scriptComponent.script2kLoader
        execEngine = loader.execEngine
        conductorImpl = loader.conductorImpl
        assertThat(execEngine).isNotNull()
        assertThat(conductorImpl).isNotNull()
        assertThat(loader.scriptHost).isNotNull()
        return mainComponent
    }

    fun loadScriptFromFile(
        scriptName: String,
    ): ResultWithDiagnostics<EvaluationResult> {
        assertThat(loader).isNotNull()
        loader.loadScriptFromFile(scriptName)
        execEngine.onExecStart()
        return loader.result
    }

    fun loadScriptFromText(
        scriptText: String,
        scriptName: String = "local",
    ): ResultWithDiagnostics<EvaluationResult> {
        assertThat(loader).isNotNull()
        val prefix = """
            import com.alfray.conductor.v2.script.dsl.*
        """.trimIndent()
        loader.loadScriptFromText(scriptName, prefix + "\n" + scriptText)
        try {
            execEngine.onExecStart()
        } catch (t: Throwable) {
            loader.addError(t)
        }
        return loader.result
    }

    fun assertResultNoError() {
        assertThat(loader.getResultErrors().joinToString("\n")).isEmpty()
    }

    fun assertResultContainsError(expected: String) {
        assertThat(loader.getResultErrors().joinToString("\n")).contains(expected)
    }
}
