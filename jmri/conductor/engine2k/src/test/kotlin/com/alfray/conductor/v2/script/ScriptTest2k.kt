package com.alfray.conductor.v2.script

import com.alfray.conductor.v2.Script2kLoader
import com.alfray.conductor.v2.script.impl.ActiveRoute
import com.alfray.conductor.v2.script.impl.RouteIdle
import com.alfray.conductor.v2.script.impl.RouteSequence
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
        val prefix = """
            import com.alfray.conductor.v2.script.seconds
            import com.alfray.conductor.v2.script.speed
        """.trimIndent()
        loader.loadScriptFromText(scriptName, prefix + "\n" + scriptText)
        return loader.result
    }

    private fun assertResultNoError() {
        assertThat(loader.getResultErrors().joinToString("\n")).isEmpty()
    }

    @Test
    fun testSampleV2() {
        loadScriptFromFile("sample_v2")
        assertResultNoError()
    }

    @Test
    fun testScript45() {
        loadScriptFromFile("script_v45_v2")
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
        assertThat(conductorImpl.timers.map { it.delay }).containsExactly(5.seconds, 15.seconds)
    }


    @Test
    fun testDontLeakImplementationDetails_BaseVars() {
        loadScriptFromText(scriptText =
        """
        val Sensor1 = sensor("S01")
        println("varName is ${"$"}{Sensor1.varName}")
        """.trimIndent()
        )

        assertThat(loader.getResultErrors()).contains("ERROR Unresolved reference: varName (local.conductor.kts:4:31)")
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

        assertThat(t.speed).isEqualTo(0.speed)
        assertThat(t.stopped).isTrue()
        t.forward(5.speed)
        assertThat(t.speed).isEqualTo(5.speed)
        assertThat(t.stopped).isFalse()
        t.reverse(15.speed)
        assertThat(t.speed).isEqualTo(15.speed.reverse())
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
            "@timer@5", "@timer@15")

        val t = conductorImpl.timers[0]
        assertThat(t.name).isEqualTo("@timer@5")
        assertThat(t.delay).isEqualTo(5.seconds)
        assertThat(t.started).isFalse()
        assertThat(t.active).isFalse()
        assertThat(!t).isTrue()

        t.start()
        assertThat(t.started).isTrue()
        t.stop()
        assertThat(t.started).isFalse()
        t.start()
        t.reset()
        assertThat(t.started).isFalse()

        t.reset()
        t.start()
        t.update(1.0)
        assertThat(t.active).isFalse()
        t.update(6.0)
        assertThat(t.active).isTrue()

        // A stopped timer does not update and does not become active
        t.reset()
        t.start()
        t.update(1.0)
        assertThat(t.active).isFalse()
        t.stop()
        t.update(6.0)
        assertThat(t.active).isFalse()
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

    @Test
    fun testRuleTurnout() {
        loadScriptFromText(scriptText =
        """
        val Turnout1 = turnout("NT1")
        val Sensor1 = sensor("S01")
        fun ResetTurnouts() { Turnout1.reverse() }
        on {  Sensor1.active } then { Turnout1.normal()  }
        on { !Sensor1        } then { ResetTurnouts()    }
        """.trimIndent()
        )
        assertResultNoError()

        assertThat(conductorImpl.rules).hasSize(2)

        val turnout1 = conductorImpl.turnouts["NT1"]!!
        val sensor1  = conductorImpl.sensors["S01"]!!

        assertThat(sensor1.active).isFalse()
        assertThat(turnout1.normal).isTrue()

        sensor1.active(true)
        execEngine.executeRules()
        assertThat(turnout1.normal).isTrue()

        sensor1.active(false)
        execEngine.executeRules()
        assertThat(turnout1.normal).isFalse()
    }

    @Test
    fun testRuleThrottle() {
        loadScriptFromText(scriptText =
        """
        val Train1  = throttle(1001)
        val Train2  = throttle(1002)
        val Sensor1 = sensor("S01")
        val Sensor2 = sensor("S02")
        // Syntax using an action as a function
        on { !Sensor1 } then { Train1.stop()  }
        on {  Sensor1.active &&  Sensor2.active } then { Train1.forward(5.speed) }
        on {  Sensor1.active && !Sensor2        } then { Train1.reverse(7.speed) }
        on { Train1.forward } then { Train1.light(true); Train1.horn(); Train1.f1(true) }
        on { Train1.stopped } then { Train1.light(false); Train1.horn(); Train1.f1(false) }
        // Properties are for conditions, and functions for actions.
        on { Train1.forward } then { Train2.forward(42.speed) }
        on { Train1.reverse } then { Train2.reverse(43.speed) }
        // Stop must be a function as it has no value, it cannot be a property.
        on { Train1.stopped } then { Train2.stop() }
        """.trimIndent()
        )
        assertResultNoError()

        assertThat(conductorImpl.rules).hasSize(8)

        val train1 = conductorImpl.throttles[1001]!!
        val train2 = conductorImpl.throttles[1002]!!
        val sensor1 = conductorImpl.sensors["S01"]!!
        val sensor2 = conductorImpl.sensors["S02"]!!

        assertThat(train1.speed).isEqualTo(0.speed)
        assertThat(train2.speed).isEqualTo(0.speed)

        sensor1.active(false)
        execEngine.executeRules()
        assertThat(train1.speed).isEqualTo(0.speed)
        assertThat(train1.light).isEqualTo(false)
        assertThat(train1.f1).isEqualTo(false)
        assertThat(train2.speed).isEqualTo(0.speed)

        // Note: actions are always executed after all conditions are checked. Thus
        // changing the throttle speed does _not_ change conditions in same loop,
        // it only changes conditions in the next loop. This ensures eval consistency.
        sensor1.active(true)
        sensor2.active(true)
        execEngine.executeRules()
        assertThat(train1.speed).isEqualTo(5.speed)
        assertThat(train2.speed).isEqualTo(0.speed)
        // train1.forward condition is not active yet until the next execution pass.
        execEngine.executeRules()
        assertThat(train1.light).isEqualTo(true)
        assertThat(train1.f1).isEqualTo(true)
        assertThat(train2.speed).isEqualTo(42.speed)

        sensor1.active(true)
        sensor2.active(false)
        execEngine.executeRules()
        assertThat(train1.speed).isEqualTo(7.speed.reverse())
        execEngine.executeRules()
        assertThat(train2.speed).isEqualTo(43.speed.reverse())

        sensor1.active(false)
        execEngine.executeRules()
        assertThat(train1.speed).isEqualTo(0.speed)
        execEngine.executeRules()
        assertThat(train2.speed).isEqualTo(0.speed)
    }

    @Test
    fun testRoute() {
        loadScriptFromText(scriptText =
        """
        val Train1  = throttle(1001)
        val Block1  = block("B01")
        val Block2  = block("B02")
        val Route_Idle = route.idle()
        val Route_Seq = route.sequence {
            throttle = Train1
            timeout = 42
            val node1 = node(Block1) { }
            val node2 = node(Block2) { }
            nodes = listOf(
                listOf(node1, node2),
                listOf(node2, node1))
        }
        val Routes = activeRoute {
            routes = listOf(Route_Idle, Route_Seq)
        }
        """.trimIndent()
        )
        assertResultNoError()

        assertThat(conductorImpl.rules).hasSize(0)

        // val train1 = conductorImpl.throttles[1001]
        // val block1 = conductorImpl.sensors["B01"]!!
        // val block2 = conductorImpl.sensors["B02"]!!

        assertThat(conductorImpl.activeRoutes).hasSize(1)
        val active = conductorImpl.activeRoutes[0] as ActiveRoute
        assertThat(active.routes).hasSize(2)
        assertThat(active.routes[0]).isInstanceOf(RouteIdle::class.java)
        assertThat(active.routes[1]).isInstanceOf(RouteSequence::class.java)
        val seq = active.routes[1] as RouteSequence
        assertThat(seq.throttle.dccAddress).isEqualTo(1001)
        assertThat(seq.timeout).isEqualTo(42)
        assertThat(seq.nodes).isNotEmpty()
    }
}
