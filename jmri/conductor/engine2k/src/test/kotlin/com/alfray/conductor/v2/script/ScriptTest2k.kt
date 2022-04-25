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
    fun testVarSensor() {
        loadScriptFromFile("sample_v2")
        assertResultNoError()

        assertThat(conductorImpl.sensors).containsKey("NS829")
        assertThat(conductorImpl.sensors["NS829"]).isSameInstanceAs(conductorImpl.sensor("NS829"))

        val s = conductorImpl.sensors["NS829"]!!
        assertThat(s.systemName).isEqualTo("NS829")
        assertThat(s.active).isFalse()
        s.active(true)
        assertThat(s.active).isTrue()
        assertThat(!s).isFalse()
    }

    @Test
    fun testVarBlock() {
        loadScriptFromFile("sample_v2")
        assertResultNoError()

        assertThat(conductorImpl.blocks).containsKey("NS768")
        assertThat(conductorImpl.blocks).containsKey("NS769")
        assertThat(conductorImpl.blocks["NS768"]).isSameInstanceAs(conductorImpl.block("NS768"))

        val b = conductorImpl.blocks["NS768"]!!
        assertThat(b.systemName).isEqualTo("NS768")
        assertThat(b.active).isFalse()
        b.active(true)
        assertThat(b.active).isTrue()
        assertThat(!b).isFalse()
    }

    @Test
    fun testVarTurnout() {
        loadScriptFromFile("sample_v2")
        assertResultNoError()

        assertThat(conductorImpl.turnouts).containsKey("NT311")
        assertThat(conductorImpl.turnouts["NT311"]).isSameInstanceAs(conductorImpl.turnout("NT311"))

        val t = conductorImpl.turnouts["NT311"]!!
        assertThat(t.systemName).isEqualTo("NT311")
        assertThat(t.normal).isTrue()
        assertThat(t.active).isFalse()
        assertThat(!t).isTrue()

        t.reverse()
        assertThat(t.normal).isFalse()
        assertThat(t.active).isTrue()
        assertThat(!t).isFalse()

        t.normal()
        assertThat(t.normal).isTrue()
        assertThat(t.active).isFalse()
        assertThat(!t).isTrue()
    }

    @Test
    fun testVarThrottle() {
        loadScriptFromFile("sample_v2")
        assertResultNoError()

        assertThat(conductorImpl.throttles).containsKey(1001)
        assertThat(conductorImpl.throttles[1001]).isSameInstanceAs(conductorImpl.throttle(1001))

        val t = conductorImpl.throttles[1001]!!
        assertThat(t.dccAddress).isEqualTo(1001)

        assertThat(t.speed).isEqualTo(0)
        assertThat(t.stopped).isTrue()
        t.forward(5)
        assertThat(t.speed).isEqualTo(5)
        assertThat(t.stopped).isFalse()
        t.reverse(15)
        assertThat(t.speed).isEqualTo(-15)
        assertThat(t.stopped).isFalse()

        assertThat(t.light).isFalse()
        t.light(true)
        assertThat(t.light).isTrue()
        t.light(false)
        assertThat(t.light).isFalse()

        assertThat(t.sound).isFalse()
        t.sound(true)
        assertThat(t.sound).isTrue()
        t.sound(false)
        assertThat(t.sound).isFalse()

        assertThat(t.f.f).isEqualTo(0b00000000000)
        assertThat(t.f0).isFalse()
        assertThat(t.f1).isFalse()
        assertThat(t.f2).isFalse()
        assertThat(t.f3).isFalse()
        assertThat(t.f4).isFalse()
        assertThat(t.f5).isFalse()
        assertThat(t.f6).isFalse()
        assertThat(t.f7).isFalse()
        assertThat(t.f8).isFalse()
        assertThat(t.f9).isFalse()

        t.f1(true)
        assertThat(t.f1).isTrue()
        assertThat(t.f.f).isEqualTo(0b00000000010)
        t.f0(true)
        t.f1(false)
        t.f9(true)
        assertThat(t.f0).isTrue()
        assertThat(t.f1).isFalse()
        assertThat(t.f9).isTrue()
        assertThat(t.f.f).isEqualTo(0b01000000001)
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
