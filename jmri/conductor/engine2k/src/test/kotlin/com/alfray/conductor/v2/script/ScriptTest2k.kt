package com.alfray.conductor.v2.script

import com.alfray.conductor.v2.host.ConductorScriptHost
import com.alfray.conductor.v2.script.impl.SvgMap
import com.alfray.conductor.v2.script.impl.SvgMapBuilder
import com.google.common.io.Resources
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.host.UrlScriptSource

@Suppress("UnstableApiUsage")
class ScriptTest2k {

    private lateinit var scriptHost: ConductorScriptHost
    private lateinit var conductorImpl: ConductorImpl
    private lateinit var execEngine: ExecEngine

    @Before
    fun setUp() {
    }

    @Test
    fun emptyTest() {
        val a = 1
        assertThat(a+1).isEqualTo(2)
    }

    private fun loadScriptFromFile(scriptName: String): ResultWithDiagnostics<EvaluationResult> {
        val scriptPath = "v2/script/$scriptName.conductor.kts"
        val scriptUrl = Resources.getResource(scriptPath)!!
        val source = UrlScriptSource(scriptUrl)
        return loadScript(source)
    }

    private fun loadScriptFromText(scriptName: String = "local", scriptText: String): ResultWithDiagnostics<EvaluationResult> {
        val source = StringScriptSource(scriptText, scriptName)
        return loadScript(source)
    }

    private fun loadScript(source: SourceCode): ResultWithDiagnostics<EvaluationResult> {
        conductorImpl = ConductorImpl()
        scriptHost = ConductorScriptHost()
        execEngine = ExecEngine(conductorImpl)
        return scriptHost.eval(source, conductorImpl)
    }

    private fun getResultErrors(result: ResultWithDiagnostics<EvaluationResult>) : List<String> =
        result.reports
            .filter { it.severity != ScriptDiagnostic.Severity.DEBUG }
            .map { it.toString() }


    private fun assertResultNoError(result: ResultWithDiagnostics<EvaluationResult>) {
        assertThat(getResultErrors(result).joinToString("\n")).isEmpty()
    }

    @Test
    fun testLoadScriptAndEval() {
        val result = loadScriptFromFile("sample_v2")
        assertResultNoError(result)
    }

    @Test
    fun testVariables() {
        val result = loadScriptFromFile("sample_v2")
        assertResultNoError(result)

        assertThat(conductorImpl.blocks.keys).containsExactly("NS768", "NS769")
        assertThat(conductorImpl.sensors.keys).containsExactly("NS829")
        assertThat(conductorImpl.turnouts.keys).containsExactly("NT311", "NT312")
        assertThat(conductorImpl.throttles.keys).containsExactly(1001, 2001)
        assertThat(conductorImpl.timers.map { it.seconds }).containsExactly(5, 15)
    }


    @Test
    fun testDontLeakImplementationDetails_BaseVars() {
        val result = loadScriptFromText(scriptText =
        """
        val Sensor1 = sensor("S01")
        println("varName is ${"$"}{Sensor1.varName}")
        """.trimIndent()
        )

        assertThat(getResultErrors(result)).contains("ERROR Unresolved reference: varName (local.conductor.kts:2:31)")
    }

    @Test
    fun testVarBlock() {
        val result = loadScriptFromFile("sample_v2")
        assertResultNoError(result)

        assertThat(conductorImpl.blocks).containsKey("NS768")
        assertThat(conductorImpl.blocks).containsKey("NS769")
        assertThat(conductorImpl.blocks["NS768"]).isSameInstanceAs(conductorImpl.block("NS768"))
    }

    @Test
    fun testVarSensor() {
        val result = loadScriptFromFile("sample_v2")
        assertResultNoError(result)

        assertThat(conductorImpl.sensors).containsKey("NS829")
        assertThat(conductorImpl.sensors["NS829"]).isSameInstanceAs(conductorImpl.sensor("NS829"))
    }

    @Test
    fun testVarTurnout() {
        val result = loadScriptFromFile("sample_v2")
        assertResultNoError(result)

        assertThat(conductorImpl.turnouts).containsKey("NT311")
        assertThat(conductorImpl.turnouts["NT311"]).isSameInstanceAs(conductorImpl.turnout("NT311"))
    }

    @Test
    fun testVarThrottle() {
        val result = loadScriptFromFile("sample_v2")
        assertResultNoError(result)

        assertThat(conductorImpl.throttles).containsKey(1001)
        assertThat(conductorImpl.throttles[1001]).isSameInstanceAs(conductorImpl.throttle(1001))
    }

    @Test
    fun testVarTimer() {
        val result = loadScriptFromFile("sample_v2")
        assertResultNoError(result)

        assertThat(conductorImpl.timers.map { it.name }).containsExactly("@timer@5", "@timer@15")
    }

    @Test
    fun testMapInfo() {
        val result = loadScriptFromFile("sample_v2")
        assertResultNoError(result)

        assertThat(conductorImpl.svgMaps).containsExactly(
            "Mainline",
            SvgMapBuilder("Mainline", "Map 1.svg").create()
        )
    }

    @Test
    fun testGlobalOnRules() {
        val result = loadScriptFromText(scriptText =
        """
        val S1 = sensor("S1")
        val T1 = turnout("T1")
        on { !S1 } then { T1.reverse() }
        on {  S1 } then { T1.normal() }
        """.trimIndent()
        )
        assertResultNoError(result)

        assertThat(conductorImpl.rules).hasSize(2)

        val s1 = conductorImpl.sensors["S1"]!!
        val t1 = conductorImpl.turnouts["T1"]!!
        assertThat(t1.normal).isTrue()

        s1.active(false)
        execEngine.executeRules()
        assertThat(t1.normal).isFalse()

        s1.active(true)
        execEngine.executeRules()
        assertThat(t1.normal).isTrue()
    }

}
