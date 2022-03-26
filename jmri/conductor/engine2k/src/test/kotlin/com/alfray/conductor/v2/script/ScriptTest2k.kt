package com.alfray.conductor.v2.script

import com.alfray.conductor.v2.host.ConductorScriptHost
import com.google.common.io.Resources
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.host.UrlScriptSource

@Suppress("UnstableApiUsage")
class ScriptTest2k {

    private lateinit var scriptHost: ConductorScriptHost
    private lateinit var conductorImpl: ConductorImpl

    @Before
    fun setUp() {
    }

    @Test
    fun emptyTest() {
        val a = 1
        assertThat(a+1).isEqualTo(2)
    }

    private fun loadScriptAndEval(scriptName: String) {
        val scriptPath = "v2/script/$scriptName.conductor.kts"
        val scriptUrl = Resources.getResource(scriptPath)!!
        val source = UrlScriptSource(scriptUrl)

        conductorImpl = ConductorImpl()
        scriptHost = ConductorScriptHost()
        val result = scriptHost.eval(source, conductorImpl)
        assertThat(result.reports
            .filter { it.severity != ScriptDiagnostic.Severity.DEBUG }
            .joinToString("\n")).isEmpty()
    }

    @Test
    fun testLoadScriptAndEval() {
        loadScriptAndEval("sample_v2")
    }

    @Test
    fun testVariables() {
        loadScriptAndEval("sample_v2")

        assertThat(conductorImpl.blocks.keys).containsExactly("NS768", "NS769")
        assertThat(conductorImpl.sensors.keys).containsExactly("NS829")
        assertThat(conductorImpl.turnouts.keys).containsExactly("NT311", "NT312")
        assertThat(conductorImpl.throttles.keys).containsExactly(1001, 2001)
        assertThat(conductorImpl.timers.map { it.seconds }).containsExactly(5, 15)
    }
}
