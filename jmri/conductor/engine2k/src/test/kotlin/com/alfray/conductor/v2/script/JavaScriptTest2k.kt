package com.alfray.conductor.v2.script

import com.alfray.conductor.v2.host.JavaScriptEval
import com.google.common.io.Resources
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.host.UrlScriptSource

//@Suppress("UnstableApiUsage")
class JavaScriptTest2k {

    private val scriptEval = JavaScriptEval()
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
        loadScriptFromFile("sample_v2")
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
