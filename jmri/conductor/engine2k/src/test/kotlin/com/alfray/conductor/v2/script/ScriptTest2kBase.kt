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
import com.alfray.conductor.v2.DaggerITestComponent2k
import com.alfray.conductor.v2.ITestComponent2k
import com.alfray.conductor.v2.Script2kLoader
import com.alfray.conductor.v2.dagger.Script2kContext
import com.google.common.truth.Truth.assertThat
import javax.inject.Inject
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics

open class ScriptTest2kBase {
    internal val jmriProvider = FakeJmriProvider()
    @Inject internal lateinit var context: Script2kContext

    internal lateinit var loader: Script2kLoader
    internal lateinit var conductorImpl: ConductorImpl
    internal lateinit var execEngine: ExecEngine2k

    fun createComponent(): ITestComponent2k {
        val mainComponent = DaggerITestComponent2k
            .factory()
            .createComponent(jmriProvider)
        mainComponent.inject(this)
        val scriptComponent = context.createComponent()
        loader = scriptComponent.script2kLoader
        execEngine = loader.execEngine
        conductorImpl = loader.conductorImpl
        assertThat(execEngine).isNotNull()
        assertThat(conductorImpl).isNotNull()
        assertThat(loader.scriptHost).isNotNull()
        return mainComponent
    }

    fun loadScriptFromFile(scriptName: String): ResultWithDiagnostics<EvaluationResult> {
        assertThat(loader).isNotNull()
        loader.loadScriptFromFile(scriptName)
        return loader.result
    }

    fun loadScriptFromText(scriptName: String = "local", scriptText: String): ResultWithDiagnostics<EvaluationResult> {
        assertThat(loader).isNotNull()
        val prefix = """
            import com.alfray.conductor.v2.script.dsl.*
        """.trimIndent()
        loader.loadScriptFromText(scriptName, prefix + "\n" + scriptText)
        return loader.result
    }

    fun assertResultNoError() {
        assertThat(loader.getResultErrors().joinToString("\n")).isEmpty()
    }
}
