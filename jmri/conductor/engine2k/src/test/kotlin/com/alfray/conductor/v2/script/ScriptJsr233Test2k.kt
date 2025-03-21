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

import com.alfray.conductor.v2.host.Jsr233ScriptEval
import com.alfray.conductor.v2.script.dsl.IConductor
import com.google.common.io.Resources
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.host.UrlScriptSource

/**
 * Prototype testing the use of [Jsr233ScriptEval] to load, parse, and exec a kts script.
 * Doing so indirectly accesses the Kotlin Scripting Host using the JSR 233 API.
 * This has been deprecated in favor of using ConductorScriptHost instead; see [ScriptTest2kBase].
 * This test uses script_test1.conductor.kts.
 */
class ScriptJsr233Test2k {
    private val scriptEval = Jsr233ScriptEval()
    private lateinit var conductorImpl: ConductorImpl

    @Before
    fun setUp() {
    }

    private fun loadScriptFromFile(scriptName: String) : IConductor {
        val scriptPath = "v2/script/$scriptName.conductor.kts"
        val scriptUrl = Resources.getResource(scriptPath)!!
        val source = UrlScriptSource(scriptUrl)
        return loadScript(source)
    }

    private fun loadScriptFromText(scriptName: String = "local", scriptText: String) : IConductor {
        val source = StringScriptSource(scriptText, scriptName)
        return loadScript(source)
    }

    private fun loadScript(source: SourceCode) : IConductor {
        val impl = scriptEval.eval(source)
        conductorImpl = impl as ConductorImpl
        return impl
    }

    @Ignore
    @Test
    fun testLoadScriptAndEval() {
        loadScriptFromFile("script_test1")
        assertThat(conductorImpl).isNotNull()
    }

    @Ignore
    @Test
    fun testEvalDirect() {
        loadScriptFromText(scriptText = """
            import com.alfray.conductor.v2.script.ConductorImpl
            ConductorImpl().apply {
                val s1 = sensor("S01")
                val s2 = sensor("S01")
            }
        """.trimIndent())
        assertThat(conductorImpl).isNotNull()
        assertThat(conductorImpl.sensors.keys).contains("S01")
    }
}
