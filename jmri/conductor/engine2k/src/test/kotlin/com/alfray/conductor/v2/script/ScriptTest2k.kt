package com.alfray.conductor.v2.script

import com.alfray.conductor.v2.Script2kLoader
import com.alfray.conductor.v2.script.impl.SvgMapBuilder
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics

@Suppress("UnstableApiUsage")
class ScriptTest2k {
    private lateinit var loader: Script2kLoader
    private val conductorImpl: ConductorImpl
        get() = loader.conductorImpl
    private val execEngine: ExecEngine
        get() = loader.execEngine

    @Before
    fun setUp() {
    }

    @Test
    fun emptyTest() {
        val a = 1
        assertThat(a+1).isEqualTo(2)
    }

    private fun loadScriptFromFile(scriptName: String): ResultWithDiagnostics<EvaluationResult> {
        loader = Script2kLoader()
        loader.loadScriptFromFile(scriptName)
        return loader.result
    }

    private fun loadScriptFromText(scriptName: String = "local", scriptText: String): ResultWithDiagnostics<EvaluationResult> {
        loader = Script2kLoader()
        loader.loadScriptFromText(scriptName, scriptText)
        return loader.result
    }

    private fun assertResultNoError() {
        assertThat(loader.getResultErrors().joinToString("\n")).isEmpty()
    }

    @Test
    fun testLoadScriptAndEval() {
        loadScriptFromFile("sample_v2")
        assertResultNoError()
    }

    @Test
    fun testVariables() {
        loadScriptFromFile("sample_v2")
        assertResultNoError()

        assertThat(conductorImpl.blocks.keys).containsExactly("NS768", "NS769")
        assertThat(conductorImpl.sensors.keys).containsExactly("NS829")
        assertThat(conductorImpl.turnouts.keys).containsExactly("NT311", "NT312")
        assertThat(conductorImpl.throttles.keys).containsExactly(1001, 2001)
        assertThat(conductorImpl.timers.map { it.seconds }).containsExactly(5, 15, 42, 5, 7, 9)
    }


    @Test
    fun testDontLeakImplementationDetails_BaseVars() {
        loadScriptFromText(scriptText =
        """
        val Sensor1 = sensor("S01")
        println("varName is ${"$"}{Sensor1.varName}")
        """.trimIndent()
        )

        assertThat(loader.getResultErrors()).contains("ERROR Unresolved reference: varName (local.conductor.kts:2:31)")
    }

    @Test
    fun testVarBlock() {
        loadScriptFromFile("sample_v2")
        assertResultNoError()

        assertThat(conductorImpl.blocks).containsKey("NS768")
        assertThat(conductorImpl.blocks).containsKey("NS769")
        assertThat(conductorImpl.blocks["NS768"]).isSameInstanceAs(conductorImpl.block("NS768"))
    }

    @Test
    fun testVarSensor() {
        loadScriptFromFile("sample_v2")
        assertResultNoError()

        assertThat(conductorImpl.sensors).containsKey("NS829")
        assertThat(conductorImpl.sensors["NS829"]).isSameInstanceAs(conductorImpl.sensor("NS829"))
    }

    @Test
    fun testVarTurnout() {
        loadScriptFromFile("sample_v2")
        assertResultNoError()

        assertThat(conductorImpl.turnouts).containsKey("NT311")
        assertThat(conductorImpl.turnouts["NT311"]).isSameInstanceAs(conductorImpl.turnout("NT311"))
    }

    @Test
    fun testVarThrottle() {
        loadScriptFromFile("sample_v2")
        assertResultNoError()

        assertThat(conductorImpl.throttles).containsKey(1001)
        assertThat(conductorImpl.throttles[1001]).isSameInstanceAs(conductorImpl.throttle(1001))
    }

    @Test
    fun testVarTimer() {
        loadScriptFromFile("sample_v2")
        assertResultNoError()

        assertThat(conductorImpl.timers.map { it.name }).containsExactly(
            "@timer@5", "@timer@15", "@timer@42", "@timer@5", "@timer@7", "@timer@9")
    }

    @Test
    fun testMapInfo() {
        loadScriptFromFile("sample_v2")
        assertResultNoError()

        assertThat(conductorImpl.svgMaps).containsExactly(
            "Mainline",
            SvgMapBuilder("Mainline", "Map 1.svg").create()
        )
    }

    @Test
    fun testGlobalOnRules() {
        loadScriptFromText(scriptText =
        """
        val S1 = sensor("S1")
        val T1 = turnout("T1")
        on { !S1 } then { T1.reverse() }
        on {  S1 } then { T1.normal() }
        """.trimIndent()
        )
        assertResultNoError()

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
