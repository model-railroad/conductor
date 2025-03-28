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

import com.alflabs.conductor.util.MqttClient
import com.alflabs.kv.IKeyValue
import com.alflabs.utils.FakeClock
import com.alfray.conductor.v2.script.dsl.IBlock
import com.alfray.conductor.v2.script.dsl.IIdleRoute
import com.alfray.conductor.v2.script.dsl.SvgMapTarget
import com.alfray.conductor.v2.script.dsl.seconds
import com.alfray.conductor.v2.script.dsl.speed
import com.alfray.conductor.v2.script.impl.Block
import com.alfray.conductor.v2.script.impl.FBits
import com.alfray.conductor.v2.script.impl.GaEvent
import com.alfray.conductor.v2.script.impl.IdleRoute
import com.alfray.conductor.v2.script.impl.JsonEvent
import com.alfray.conductor.v2.script.impl.RouteBase
import com.alfray.conductor.v2.script.impl.RoutesContainer
import com.alfray.conductor.v2.script.impl.SequenceRoute
import com.alfray.conductor.v2.script.impl.SvgMapBuilder
import com.alfray.conductor.v2.script.impl.Timer
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.After
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

/** Tests most features of the Conductor 2 DSL scripting engine. */
class ScriptDslTest2k : ScriptTest2kBase() {
    @Inject lateinit var clock: FakeClock
    @Inject lateinit var keyValue: IKeyValue
    @Inject lateinit var mockMqttClient: MqttClient
    @Inject internal lateinit var currentContext: CurrentContext
    private val testContext = ExecContext(ExecContext.Reason.GLOBAL_RULE)

    @Before
    fun setUp() {
        createComponent()
        scriptComponent.inject(this)
        fileOps.writeBytes(
            "<svg/>".toByteArray(Charsets.UTF_8),
            fileOps.toFile("v2", "script", "Map 1.svg"))
    }

    @After
    fun tearDown() {
        currentContext.resetContext()
    }

    @Test
    fun testScriptWithError() {
        loadScriptFromText(scriptText =
        """
        Sensor1 = sensor("S01")
        val T1 = throttle 1001
        val Routes = routes
        """.trimIndent()
        )
        // TBD... can we make this error less cryptic?
        assertResultContainsError("""
            ERROR Property getter or setter expected (local.conductor.kts:3:19)
            ERROR Unresolved reference: Sensor1 (local.conductor.kts:2:1)
            ERROR Function invocation 'throttle(...)' expected (local.conductor.kts:3:10)
            ERROR None of the following functions can be called with the arguments supplied: 
        """.trimIndent())
    }

    @Test
    fun testSampleV2() {
        loadScriptFromFile("script_test1")
        assertResultNoError()
    }

    @Test
    fun testVariables() {
        loadScriptFromFile("script_test1")
        assertResultNoError()

        assertThat(conductorImpl.blocks.keys).containsExactly("NS768", "NS769")
        assertThat(conductorImpl.sensors.keys).containsExactly("NS829")
        assertThat(conductorImpl.turnouts.keys).containsExactly("NT311", "NT312")
        assertThat(conductorImpl.throttles.keys).containsExactly(1001, 2001)
        assertThat(conductorImpl.timers.map { it.delay }).containsExactly(
            5.seconds,
            15.seconds,
            120.seconds,
            )
    }


    @Test
    fun testDontLeakImplementationDetails_baseVars() {
        loadScriptFromText(scriptText =
        """
        val Sensor1 = sensor("S01")
        println("varName is ${"$"}{Sensor1.varName}")
        """.trimIndent()
        )

        assertThat(loader.getResultErrors()).contains("ERROR Unresolved reference: varName (local.conductor.kts:3:31)")
    }

    @Test
    fun testVarSensor() {
        loadScriptFromFile("script_test1")
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
        loadScriptFromFile("script_test1")
        assertResultNoError()

        assertThat(conductorImpl.blocks).containsKey("NS768")
        assertThat(conductorImpl.blocks).containsKey("NS769")
        assertThat(conductorImpl.blocks["NS768"]).isSameInstanceAs(conductorImpl.block("NS768"))

        val b1 = conductorImpl.blocks["NS768"]!! as Block
        assertThat(b1.systemName).isEqualTo("NS768")
        assertThat(b1.name).isEqualTo("NS768")
        assertThat(b1.active).isFalse()
        b1.internalActive(true)
        execEngine.onExecHandle()

        assertThat(b1.active).isTrue()
        assertThat(!b1).isFalse()

        val b2 = conductorImpl.blocks["NS769"]!! as Block
        assertThat(b2.systemName).isEqualTo("NS769")
        assertThat(b2.name).isEqualTo("B311")
        // the blocks map only uses system names as keys, not named variables
        assertThat(conductorImpl.blocks).containsKey("NS769")
        assertThat(conductorImpl.blocks).doesNotContainKey("B311")
    }

    @Test
    fun testVarTurnout() {
        loadScriptFromFile("script_test1")
        assertResultNoError()

        assertThat(conductorImpl.turnouts).containsKey("NT311")
        assertThat(conductorImpl.turnouts["NT311"]).isSameInstanceAs(conductorImpl.turnout("NT311"))

        val t = conductorImpl.turnouts["NT311"]!!
        assertThat(t.systemName).isEqualTo("NT311")
        assertThat(t.normal).isTrue()
        assertThat(t.active).isTrue()
        assertThat(!t).isFalse()

        t.reverse()
        assertThat(t.normal).isFalse()
        assertThat(t.active).isFalse()
        assertThat(!t).isTrue()

        t.normal()
        assertThat(t.normal).isTrue()
        assertThat(t.active).isTrue()
        assertThat(!t).isFalse()
    }

    @Test
    fun testVarThrottle() {
        loadScriptFromFile("script_test1")
        assertResultNoError()
        currentContext.changeContext(testContext)

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

        assertThat((t.f as FBits).f).isEqualTo(0b00000000000)
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
        assertThat((t.f as FBits).f).isEqualTo(0b00000000010)
        t.f0(true)
        t.f1(false)
        t.f9(true)
        assertThat(t.f0).isTrue()
        assertThat(t.f1).isFalse()
        assertThat(t.f9).isTrue()
        assertThat((t.f as FBits).f).isEqualTo(0b01000000001)
    }

    @Test
    fun testVarThrottle_withBuilder() {
        loadScriptFromText(scriptText =
        """
        val S1 = sensor("S01")
        val T1 = throttle(1001) {
            onLight { b -> throttle.f10( b) }
            onSound { b -> throttle.f18(!b) }
            onBell  { throttle.f11(it) }
        }
        on {  S1 } then {
            T1.light(true)            
            T1.sound(true)            
            T1.bell(true)            
        }
        on { !S1 } then {
            T1.light(false)
            T1.sound(false)
            T1.bell(false)
        }
        """.trimIndent()
        )
        assertResultNoError()

        val t1 = conductorImpl.throttles[1001]!!
        val s1 = conductorImpl.sensors["S01"]!!

        assertThat(t1.name).isEqualTo("Throttle-1001")

        s1.active(false)
        execEngine.onExecHandle()
        assertThat(t1.light).isFalse()
        assertThat(t1.sound).isFalse()
        assertThat(t1.f10).isFalse()
        assertThat(t1.f11).isFalse()
        assertThat(t1.f18).isTrue()

        s1.active(true)
        execEngine.onExecHandle()
        assertThat(t1.light).isTrue()
        assertThat(t1.sound).isTrue()
        assertThat(t1.f10).isTrue()
        assertThat(t1.f11).isTrue()
        assertThat(t1.f18).isFalse()

        s1.active(false)
        execEngine.onExecHandle()
        assertThat(t1.light).isFalse()
        assertThat(t1.sound).isFalse()
        assertThat(t1.f10).isFalse()
        assertThat(t1.f11).isFalse()
        assertThat(t1.f18).isTrue()
    }

    @Test
    fun testVarThrottle_withBuilder_andNamed() {
        loadScriptFromText(scriptText =
        """
        val S1 = sensor("S01")
        val T1 = throttle(1001) {
            onLight { b -> throttle.f10( b) }
            onSound { b -> throttle.f18(!b) }
            onBell  { throttle.f11(it) }
        } named "Throttle1"
        """.trimIndent()
        )
        assertResultNoError()

        val t1 = conductorImpl.throttles[1001]!!
        assertThat(t1.name).isEqualTo("Throttle1")
    }

    @Test
    fun testVarThrottle_withBuilderName() {
        loadScriptFromText(scriptText =
        """
        val S1 = sensor("S01")
        val T1 = throttle(1001) {
            name = "Throttle1"
            onLight { b -> throttle.f10( b) }
            onSound { b -> throttle.f18(!b) }
            onBell  { throttle.f11(it) }
        }
        """.trimIndent()
        )
        assertResultNoError()

        val t1 = conductorImpl.throttles[1001]!!
        assertThat(t1.name).isEqualTo("Throttle1")
    }

    @Test
    fun testVarThrottle_withBuilderName_andNamed_isError() {
        loadScriptFromText(scriptText =
        """
        val S1 = sensor("S01")
        val T1 = throttle(1001) {
            name = "Throttle1"
            onLight { b -> throttle.f10( b) }
            onSound { b -> throttle.f18(!b) }
            onBell  { throttle.f11(it) }
        } named "Throttle2"
        """.trimIndent()
        )
        assertResultContainsError("Variable name already set to 'Throttle1', cannot be changed to 'Throttle2'")
    }

    @Test
    fun testVarThrottle_named() {
        loadScriptFromText(scriptText =
        """
        val T1 = throttle(1001)
        check(T1 is IThrottle)
        val T2 = throttle(1002) named "Engine#2"
        check(T2 is IVarName)
        check(T2 is IThrottle)
        val T3 = throttle(1003) named "My Throttle 3!"
        check(T3 is IThrottle)        
        """.trimIndent()
        )
        assertResultNoError()

        assertThat(conductorImpl.throttles).hasSize(3)

        assertThat(conductorImpl.throttles[1001]!!.name).isEqualTo("Throttle-1001")
        assertThat(conductorImpl.throttles[1002]!!.name).isEqualTo("Engine#2")
        assertThat(conductorImpl.throttles[1003]!!.name).isEqualTo("My Throttle 3!")
    }

    @Test
    fun testVarThrottle_named_isError() {
        loadScriptFromText(scriptText =
        """
        val T1 = throttle(1001)
        val T2 = throttle(1002) named "T2" named "T2"
        val T3 = throttle(1003) named "My Throttle 3!" named "T3"
        """.trimIndent()
        )
        assertResultContainsError("Variable name already set to 'My Throttle 3!', cannot be changed to 'T3'")
    }

    @Test
    fun testVarTimer() {
        loadScriptFromFile("script_test1")
        assertResultNoError()

        assertThat(conductorImpl.timers.map { it.name }).containsExactly(
            "@timer@5", "@timer@15", "@timer@120")

        val t = conductorImpl.timers[0] as Timer
        assertThat(t.name).isEqualTo("@timer@5")
        assertThat(t.delay).isEqualTo(5.seconds)
        assertThat(t.active).isFalse()
        assertThat(!t).isTrue()

        t.start()
        clock.add(4 * 1000)
        assertThat(t.active).isFalse()
        clock.add(1 * 1000)
        assertThat(t.active).isTrue()

        t.reset()
        assertThat(t.active).isFalse()
    }

    @Test
    fun testMapInfo() {
        loadScriptFromFile("script_test1")
        assertResultNoError()

        assertThat(conductorImpl.svgMaps).containsExactly(
            SvgMapBuilder("Mainline", "Map 1.svg", SvgMapTarget.RTAC).create(),
            SvgMapBuilder("Mainline", "Map 1.svg", SvgMapTarget.Conductor).create(),
        )
    }

    @Test
    fun testGaEvent() {
        loadScriptFromFile("script_test1")
        assertResultNoError()

        assertThat(conductorImpl.analytics.lastGaEvent).isEqualTo(
            GaEvent(
                category = "Motion",
                action = "Stop",
                label = "AIU",
                user = "AIU-Motion-Counter",
            )
        )
    }

    @Test
    fun testJsonEvent() {
        loadScriptFromFile("script_test1")
        assertResultNoError()

        assertThat(conductorImpl.lastJsonEvent).isEqualTo(
            JsonEvent(
                key1 = "Depart",
                key2 = "Passenger",
                value = "value",
            )
        )
    }

    @Test
    fun testOnRules() {
        loadScriptFromText(scriptText =
        """
        val S1 = sensor("S1")
        val T1 = turnout("T1")
        on { !S1 } then { T1.reverse() }
        on {  S1 } then { T1.normal() }
        """.trimIndent()
        )
        assertResultNoError()

        val s1 = conductorImpl.sensors["S1"]!!
        val t1 = conductorImpl.turnouts["T1"]!!
        assertThat(t1.normal).isTrue()

        s1.active(false)
        execEngine.onExecHandle()
        assertThat(t1.normal).isFalse()

        s1.active(true)
        execEngine.onExecHandle()
        assertThat(t1.normal).isTrue()
    }

    @Test
    fun testOnRules_multipleSameConditions() {
        loadScriptFromText(scriptText =
        """
        val S1 = sensor("S1")
        val T1 = throttle(1001)
        val T2 = throttle(1002)
        val T3 = throttle(1003)
        var n = 1
        on { !S1 } then { T1.reverse(n++.speed) }
        on { !S1 } then { T2.reverse(n++.speed) }
        on { !S1 } then { T3.reverse(n++.speed) }
        on {  S1 } then { T1.forward(n++.speed) }
        on {  S1 } then { T2.forward(n++.speed) }
        on {  S1 } then { T3.forward(n++.speed) }
        """.trimIndent()
        )
        assertResultNoError()

        val s1 = conductorImpl.sensors["S1"]!!
        val t1 = conductorImpl.throttles[1001]!!
        val t2 = conductorImpl.throttles[1002]!!
        val t3 = conductorImpl.throttles[1003]!!
        assertThat(t1.speed).isEqualTo(0.speed)
        assertThat(t2.speed).isEqualTo(0.speed)
        assertThat(t3.speed).isEqualTo(0.speed)

        s1.active(false)
        execEngine.onExecHandle()
        execEngine.onExecHandle()
        execEngine.onExecHandle()
        assertThat(t1.speed).isEqualTo((-1).speed)
        assertThat(t2.speed).isEqualTo((-2).speed)
        assertThat(t3.speed).isEqualTo((-3).speed)

        s1.active(true)
        execEngine.onExecHandle()
        execEngine.onExecHandle()
        execEngine.onExecHandle()
        assertThat(t1.speed).isEqualTo(4.speed)
        assertThat(t2.speed).isEqualTo(5.speed)
        assertThat(t3.speed).isEqualTo(6.speed)

        s1.active(false)
        execEngine.onExecHandle()
        execEngine.onExecHandle()
        execEngine.onExecHandle()
        assertThat(t1.speed).isEqualTo((-7).speed)
        assertThat(t2.speed).isEqualTo((-8).speed)
        assertThat(t3.speed).isEqualTo((-9).speed)
    }

    @Test
    fun testOnRules_cannotBeNested() {
        loadScriptFromText(scriptText =
        """
        val S1 = sensor("S1")
        val T1 = turnout("T1")
        on { !S1 } then {
            on { S1 } then { T1.normal() }
            T1.reverse()
        }
        """.trimIndent()
        )
        assertResultNoError()

        val turnout1 = conductorImpl.turnouts["T1"]!!
        val sensor1  = conductorImpl.sensors["S1"]!!
        assertThat(turnout1.normal).isTrue()
        sensor1.active(false)

        execEngine.onExecHandle()
        assertThat(logger.string)
            .contains("ERROR: on..then rule must be defined at the top global level.")
        // execution aborted and the turnout was never thrown
        assertThat(turnout1.normal).isTrue()
    }

    @Test
    fun testOnRules_withAfterThen() {
        loadScriptFromText(scriptText =
        """
        val S1 = sensor("S1")
        val T1 = turnout("T1")
        on { !S1 } then {
            after (5.seconds) then { T1.reverse() }
        }
        """.trimIndent()
        )
        assertResultNoError()

        val turnout1 = conductorImpl.turnouts["T1"]!!
        val sensor1  = conductorImpl.sensors["S1"]!!
        assertThat(turnout1.normal).isTrue()

        // 1- on condition becomes true: action invoked, after() gets created and queued.
        sensor1.active(false)
        execEngine.onExecHandle()
        assertThat(logger.string).doesNotContain("ERROR")
        assertThat(turnout1.normal).isTrue()

        // 2- after timer gets started
        execEngine.onExecHandle()

        // 3- after timer gets activated, after..then action invoked.
        clock.add(6*1000)
        execEngine.onExecHandle()
        assertThat(logger.string).doesNotContain("ERROR")
        assertThat(turnout1.normal).isFalse()
    }

    @Test
    fun testAfterThen_notAtTopLevel() {
        loadScriptFromText(scriptText =
        """
        val T1 = turnout("T1")
        after (5.seconds) then { T1.reverse() }
        """.trimIndent()
        )
        assertResultContainsError(
            "ERROR: after..then action must be defined in an event or rule definition.")
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

        val turnout1 = conductorImpl.turnouts["NT1"]!!
        val sensor1  = conductorImpl.sensors["S01"]!!

        assertThat(sensor1.active).isFalse()
        assertThat(turnout1.normal).isTrue()

        sensor1.active(true)
        execEngine.onExecHandle()
        assertThat(turnout1.normal).isTrue()

        sensor1.active(false)
        execEngine.onExecHandle()
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

        val train1 = conductorImpl.throttles[1001]!!
        val train2 = conductorImpl.throttles[1002]!!
        val sensor1 = conductorImpl.sensors["S01"]!!
        val sensor2 = conductorImpl.sensors["S02"]!!

        assertThat(train1.speed).isEqualTo(0.speed)
        assertThat(train2.speed).isEqualTo(0.speed)

        sensor1.active(false)
        execEngine.onExecHandle()
        assertThat(train1.speed).isEqualTo(0.speed)
        assertThat(train1.light).isEqualTo(false)
        assertThat(train1.f1).isEqualTo(false)
        assertThat(train2.speed).isEqualTo(0.speed)

        // Note: actions are always executed after all conditions are checked. Thus
        // changing the throttle speed does _not_ change conditions in same loop,
        // it only changes conditions in the next loop. This ensures eval consistency.
        sensor1.active(true)
        sensor2.active(true)
        execEngine.onExecHandle()
        assertThat(train1.speed).isEqualTo(5.speed)
        assertThat(train2.speed).isEqualTo(0.speed)
        // train1.forward condition is not active yet until the next execution pass.
        execEngine.onExecHandle()
        assertThat(train1.light).isEqualTo(true)
        assertThat(train1.f1).isEqualTo(true)
        assertThat(train2.speed).isEqualTo(42.speed)

        sensor1.active(true)
        sensor2.active(false)
        execEngine.onExecHandle()
        assertThat(train1.speed).isEqualTo(7.speed.reverse())
        execEngine.onExecHandle()
        assertThat(train2.speed).isEqualTo(43.speed.reverse())

        sensor1.active(false)
        execEngine.onExecHandle()
        assertThat(train1.speed).isEqualTo(0.speed)
        execEngine.onExecHandle()
        assertThat(train2.speed).isEqualTo(0.speed)
    }

    @Test
    fun testIdleRoute() {
        loadScriptFromText(scriptText =
        """
        val Toggle = sensor("S01")
        val Routes = routes {
            name = "PA"
            toggle = Toggle
            status = { "My Idle Route" }
        }
        val Route_Idle = Routes.idle {}
        """.trimIndent()
        )
        assertResultNoError()
        assertThat(conductorImpl.routesContainers).hasSize(1)

        val routesContainer = conductorImpl.routesContainers[0]
        val route = routesContainer.active
        assertThat(route).isInstanceOf(IIdleRoute::class.java)
        assertThat(route.toString()).isEqualTo("Idle PA #0")
        assertThat(routesContainer.toString()).isEqualTo("RoutesContainer PA")

        execEngine.onExecHandle()
        execEngine.onExecHandle()

        val kv = keyValue.keys
            .sorted()
            .map { "$it=" + keyValue.getValue(it) }
            .toList()
        assertThat(kv).containsExactly(
            "M/maps={\"mapInfos\":[]}",
            "R/pa\$counter=0",
            "R/pa\$status=My Idle Route",
            "R/pa\$throttle=0",
            "R/pa\$toggle=OFF",
            "R/routes={\"routeInfos\":[{\"name\":\"PA\",\"toggleKey\":\"R/pa\$toggle\",\"statusKey\":\"R/pa\$status\",\"counterKey\":\"R/pa\$counter\",\"throttleKey\":\"R/pa\$throttle\"}]}",
            "S/S01=OFF",
            "V/\$estop-state\$=NORMAL",
            "V/conductor-time=1342",
            "V/rtac-motion=OFF",
            "V/rtac-psa-text=",
        ).inOrder()
    }


    @Test
    fun testIdleRoute_onIdle() {
        loadScriptFromText(scriptText =
        """
        val Train1  = throttle(1001)
        val Toggle = sensor("S01")
        val Routes = routes {
            name = "PA"
            toggle = Toggle
            status = { "My Idle Route" }
        }
        val Route_Idle = Routes.idle {
            onActivate {
                Train1.forward(2.speed)
            }
            onIdle {
                val s: Int = Train1.speed.speed + 1
                Train1.forward(s.speed)
            }
        }
        """.trimIndent()
        )
        assertResultNoError()
        assertThat(conductorImpl.routesContainers).hasSize(1)

        val train1 = conductorImpl.throttles[1001]!!
        assertThat(train1.speed).isEqualTo(0.speed)

        execEngine.onExecHandle()
        assertThat(train1.speed).isEqualTo(2.speed)

        execEngine.onExecHandle()
        assertThat(train1.speed).isEqualTo(3.speed)

        execEngine.onExecHandle()
        assertThat(train1.speed).isEqualTo(4.speed)
    }

    @Test
    fun testIdleRoute_error() {
        loadScriptFromText(scriptText =
        """
        val Toggle = sensor("S01")
        val Routes = routes {
            name = "PA"
            toggle = Toggle
        }
        val Route_Idle = Routes.idle()
        """.trimIndent()
        )
        assertResultContainsError(
            "ERROR No value passed for parameter 'idleRouteSpecification' (local.conductor.kts:7:")
    }

    @Test
    fun testSequenceRoute() {
        jmriProvider.getSensor("B01").isActive = true
        loadScriptFromText(
            scriptText =
            """
        val Train1  = throttle(1001)
        val Block1  = block("B01")
        val Block2  = block("B02")
        val Toggle = sensor("S01")
        val Routes = routes {
            name = "PA"
            toggle = Toggle
        }
        val Route_Idle = Routes.idle {}
        val Route_Seq = Routes.sequence {
            throttle = Train1
            maxSecondsOnBlock = 42
            val block1_fwd = node(Block1) { }
            val block2_fwd = node(Block2) { }
            val block1_rev = node(Block1) {
                onEnter {
                    routes.activate(Route_Idle)
                }
            }
            sequence = listOf(block1_fwd, block2_fwd, block1_rev)
        }
        """.trimIndent()
        )
        assertResultNoError()

        assertThat(conductorImpl.routesContainers).hasSize(1)
        val ar = conductorImpl.routesContainers[0] as RoutesContainer

        assertThat(ar.routes).hasSize(2)
        assertThat(ar.routes[0]).isInstanceOf(IdleRoute::class.java)
        assertThat(ar.routes[1]).isInstanceOf(SequenceRoute::class.java)
        val seq = ar.routes[1] as SequenceRoute
        assertThat(seq.throttle.dccAddress).isEqualTo(1001)
        assertThat(seq.maxSecondsOnBlock).isEqualTo(42)
        assertThat(seq.graph).isNotNull()

        assertThat(ar.active).isSameInstanceAs(ar.routes[0])
        ar.activate(ar.routes[1])
        assertThat(ar.active).isSameInstanceAs(ar.routes[1])

        assertThat(seq.graph.toString()).isEqualTo(
            "[{B01}=>><B02>=<>{B01}]"
        )

        assertThat(seq.toString()).isEqualTo("Sequence PA #1 (1001)")
        assertThat(ar.toString()).isEqualTo("RoutesContainer PA")

        execEngine.onExecHandle()
        execEngine.onExecHandle()

        val kv = keyValue.keys
            .sorted()
            .map { "$it=" + keyValue.getValue(it) }
            .toList()
        assertThat(kv).containsExactly(
            "D/1001=0",
            "M/maps={\"mapInfos\":[]}",
            "R/pa\$counter=1",
            "R/pa\$status=Idle",
            "R/pa\$throttle=0",
            "R/pa\$toggle=OFF",
            "R/routes={\"routeInfos\":[{\"name\":\"PA\",\"toggleKey\":\"R/pa\$toggle\",\"statusKey\":\"R/pa\$status\",\"counterKey\":\"R/pa\$counter\",\"throttleKey\":\"R/pa\$throttle\"}]}",
            "S/B01=ON",
            "S/B02=OFF",
            "S/S01=OFF",
            "V/\$estop-state\$=NORMAL",
            "V/conductor-time=1342",
            "V/rtac-motion=OFF",
            "V/rtac-psa-text=",
        ).inOrder()
    }

    @Test
    fun testRouteBranches() {
        loadScriptFromText(scriptText =
        """
        val Train1  = throttle(1001)
        val Block1  = block("B01")
        val Block2  = block("B02")
        val Block3  = block("B03")
        val Block4  = block("B04")
        val Toggle = sensor("S01")
        val Routes = routes {
            name = "PA"
            toggle = Toggle
        }
        val Route_Idle = Routes.idle {}
        val Route_Seq = Routes.sequence {
            throttle = Train1
            maxSecondsOnBlock = 42
            val block1_fwd = node(Block1) { }
            val block2_fwd = node(Block2) { }
            val block3_fwd = node(Block3) { }
            val block4_fwd = node(Block4) { }
            val block1_rev = node(Block1) {
                onEnter {
                    routes.activate(Route_Idle)
                }
            }
            sequence = listOf(block1_fwd, block2_fwd, block1_rev)
            branches += listOf(block1_fwd, block3_fwd, block4_fwd, block1_rev)
            branches += listOf(block2_fwd, block3_fwd, block1_rev)
            branches += listOf(block1_fwd, block4_fwd)
        }
        """.trimIndent()
        )
        assertResultNoError()

        val ar = conductorImpl.routesContainers[0] as RoutesContainer
        val seq = ar.routes[1] as SequenceRoute

        assertThat(seq.graph.toString()).isEqualTo(
            "[{B01}=>><B02>=<>{B01}],[{B01}->>{B03}->>{B04}->>{B01}],[{B01}->>{B04}],[<B02>->>{B03}->>{B01}]")
    }

    @Test
    fun testSequenceRoute_onRuleForbiddenInEvent() {
        jmriProvider.getSensor("B01").isActive = true
        loadScriptFromText(scriptText =
        """
        val Train1  = throttle(1001)
        val Block1  = block("B01")
        val Block2  = block("B02")
        val Toggle = sensor("S01")
        val Routes = routes {
            name = "PA"
            toggle = Toggle
        }
        val Route_Seq = Routes.sequence {
            throttle = Train1
            val block1_fwd = node(Block1) {
                onEnter {
                    on { Block2 } then { Train1.horn() }
                }
            }
            sequence = listOf(block1_fwd)
        }
        """.trimIndent()
        )
        assertResultNoError()
        assertThat(conductorImpl.routesContainers).hasSize(1)

        val route = conductorImpl.routesContainers[0].active as RouteBase
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVATED)
        execEngine.onExecHandle()
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)
        execEngine.onExecHandle()

        assertThat(logger.string)
            .contains("ERROR: on..then rule must be defined at the top global level.")
    }

    @Test
    fun testSequenceRoute_onRuleForbiddenInWhileOccupied() {
        jmriProvider.getSensor("B01").isActive = true
        jmriProvider.getSensor("B02").isActive = false
        loadScriptFromText(scriptText =
        """
        val Train1  = throttle(1001)
        val Block1  = block("B01")
        val Block2  = block("B02")
        val Toggle = sensor("S01")
        val Routes = routes {
            name = "PA"
            toggle = Toggle
        }
        var n = 1
        val Route_Seq = Routes.sequence {
            throttle = Train1
            val block1_fwd = node(Block1) {
                whileOccupied {
                    on (1.seconds) { Block2 } then { Train1.forward(n++.speed) }
                    on (2.seconds) { Block2 } then { Train1.forward(n++.speed) }
                    on             { Block2 } then { Train1.forward(n++.speed) }
                }
            }
            sequence = listOf(block1_fwd)
        }
        """.trimIndent()
        )
        assertResultNoError()
        assertThat(conductorImpl.routesContainers).hasSize(1)

        val t1 = conductorImpl.throttles[1001]!!
        val block1 = conductorImpl.blocks["B01"]!!
        val block2 = conductorImpl.blocks["B02"]!!
        val route = conductorImpl.routesContainers[0].active as RouteBase

        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVATED)
        assertThat(t1.speed).isEqualTo(0.speed)

        execEngine.onExecHandle()
        execEngine.onExecHandle()
        assertThat(block1.state).isEqualTo(IBlock.State.OCCUPIED)
        assertThat(block2.state).isEqualTo(IBlock.State.EMPTY)
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)
        assertThat(t1.speed).isEqualTo(0.speed)

        jmriProvider.getSensor("B02").isActive = true
        execEngine.onExecHandle()

        assertThat(logger.string)
            .contains("ERROR: on..then rule must be defined at the top global level.")
    }

    @Test
    fun testSequenceRoute_ifConditionWhileOccupied() {
        jmriProvider.getSensor("B01").isActive = true
        jmriProvider.getSensor("B02").isActive = false
        loadScriptFromText(scriptText =
        """
        val Train1  = throttle(1001)
        val Block1  = block("B01")
        val Block2  = block("B02")
        val Toggle = sensor("S01")
        val Routes = routes {
            name = "PA"
            toggle = Toggle
        }
        var n = 1
        val Route_Seq = Routes.sequence {
            throttle = Train1
            val block1_fwd = node(Block1) {
                whileOccupied {
                    if (Block2.active) { Train1.forward(n++.speed) }
                    if (Block2.active) { Train1.forward(n++.speed) }
                }
            }
            sequence = listOf(block1_fwd)
        }
        """.trimIndent()
        )
        assertResultNoError()
        assertThat(conductorImpl.routesContainers).hasSize(1)

        val t1 = conductorImpl.throttles[1001]!!
        val block1 = conductorImpl.blocks["B01"]!!
        val block2 = conductorImpl.blocks["B02"]!!
        val route = conductorImpl.routesContainers[0].active as RouteBase

        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVATED)
        assertThat(t1.speed).isEqualTo(0.speed)

        execEngine.onExecHandle()
        execEngine.onExecHandle()
        assertThat(block1.state).isEqualTo(IBlock.State.OCCUPIED)
        assertThat(block2.state).isEqualTo(IBlock.State.EMPTY)
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)
        assertThat(t1.speed).isEqualTo(0.speed)

        jmriProvider.getSensor("B02").isActive = true
        execEngine.onExecHandle()
        assertThat(t1.speed).isEqualTo(2.speed)
        execEngine.onExecHandle()
        assertThat(t1.speed).isEqualTo(4.speed)
        execEngine.onExecHandle()
        assertThat(t1.speed).isEqualTo(6.speed)

        jmriProvider.getSensor("B02").isActive = false
        execEngine.onExecHandle()
        assertThat(t1.speed).isEqualTo(6.speed)

        execEngine.onExecHandle()
        assertThat(t1.speed).isEqualTo(6.speed)
    }

    @Test
    fun testSequenceRoute_trainThrottleOutsideEventCallbackForbidden() {
        loadScriptFromText(scriptText =
        """
        val Train1  = throttle(1001)
        val Block1  = block("B01")
        val Toggle = sensor("S01")
        val Routes = routes {
            name = "PA"
            toggle = Toggle
        }
        val Route_Seq = Routes.sequence {
            throttle = Train1
            val block1_fwd = node(Block1) {
                Train1.forward(5.speed)
            }
            sequence = listOf(block1_fwd)
        }
        """.trimIndent()
        )
        assertResultContainsError(
            "ERROR: throttle actions must be called in an event or rule definition.")
    }

    @Test
    fun testSequenceRoute_afterOutsideEventCallbackForbidden() {
        loadScriptFromText(scriptText =
        """
        val Train1  = throttle(1001)
        val Block1  = block("B01")
        val Toggle = sensor("S01")
        val Routes = routes {
            name = "PA"
            toggle = Toggle
        }
        val Route_Seq = Routes.sequence {
            throttle = Train1
            val block1_fwd = node(Block1) {
                after (2.seconds) then {
                    Train1.forward(5.speed)
                }
            }
            sequence = listOf(block1_fwd)
        }
        """.trimIndent()
        )
        assertResultContainsError(
            "ERROR: after..then action must be defined in an event or rule definition.")
    }

    @Test
    fun testSequenceRoute_afterInWhileBlockForbidden() {
        jmriProvider.getSensor("B01").isActive = true
        loadScriptFromText(scriptText =
        """
        val Train1  = throttle(1001)
        val Block1  = block("B01")
        val Toggle = sensor("S01")
        val Routes = routes {
            name = "PA"
            toggle = Toggle
        }
        val Route_Seq = Routes.sequence {
            throttle = Train1
            val block1_fwd = node(Block1) {
                whileOccupied {
                    after (2.seconds) then {
                        Train1.forward(5.speed)
                    }
                }
            }
            sequence = listOf(block1_fwd)
        }
        """.trimIndent()
        )
        assertResultNoError()
        assertThat(conductorImpl.routesContainers).hasSize(1)

        val route = conductorImpl.routesContainers[0].active as RouteBase
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVATED)
        execEngine.onExecHandle()
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)
        execEngine.onExecHandle()

        assertThat(logger.string)
            .contains("ERROR: after..then action must be defined in an event or rule definition.")
    }

    @Test
    fun testAfterThen() {
        loadScriptFromText(scriptText =
        """
        val Train = throttle(1001)
        val S01 = sensor("S01")
        on { S01.active } then {
            Train.forward(1.speed)
            after (2.seconds) then {
                Train.forward(2.speed)
            } and_after (3.seconds) then {
                Train.forward(3.speed)
            } and_after (4.seconds) then {
                Train.forward(4.speed)
            }
        }
        """.trimIndent()
        )
        assertResultNoError()
        assertThat(conductorImpl.debugSumAfterTimers()).isEqualTo(
            ExecContext.CountTimers(numTimers = 0, numStarted = 0, numActive = 0, durationSec = 0))

        val train1 = conductorImpl.throttles[1001]!!
        val sensor1 = conductorImpl.sensors["S01"]!!

        assertThat(train1.speed).isEqualTo(0.speed)

        sensor1.active(true)
        execEngine.onExecHandle()
        assertThat(train1.speed).isEqualTo(1.speed)
        assertThat(conductorImpl.debugSumAfterTimers()).isEqualTo(
            ExecContext.CountTimers(numTimers = 3, numStarted = 0, numActive = 0, durationSec = 9))

        clock.add(1 * 1000)
        execEngine.onExecHandle()
        assertThat(train1.speed).isEqualTo(1.speed)
        assertThat(conductorImpl.debugSumAfterTimers()).isEqualTo(
            ExecContext.CountTimers(numTimers = 3, numStarted = 1, numActive = 0, durationSec = 9))

        clock.add(2 * 1000)
        execEngine.onExecHandle()
        assertThat(train1.speed).isEqualTo(2.speed)
        assertThat(conductorImpl.debugSumAfterTimers()).isEqualTo(
            ExecContext.CountTimers(numTimers = 3, numStarted = 2, numActive = 1, durationSec = 9))

        clock.add(3 * 1000)
        execEngine.onExecHandle()
        assertThat(train1.speed).isEqualTo(3.speed)
        assertThat(conductorImpl.debugSumAfterTimers()).isEqualTo(
            ExecContext.CountTimers(numTimers = 3, numStarted = 3, numActive = 2, durationSec = 9))

        clock.add(4 * 1000)
        execEngine.onExecHandle()
        assertThat(train1.speed).isEqualTo(4.speed)
        assertThat(conductorImpl.debugSumAfterTimers()).isEqualTo(
            ExecContext.CountTimers(numTimers = 3, numStarted = 3, numActive = 3, durationSec = 9))

        clock.add(5 * 1000)
        execEngine.onExecHandle()
        assertThat(train1.speed).isEqualTo(4.speed)
        assertThat(conductorImpl.debugSumAfterTimers()).isEqualTo(
            ExecContext.CountTimers(numTimers = 3, numStarted = 3, numActive = 3, durationSec = 9))
    }

    @Test
    fun testEStop() {
        loadScriptFromText(scriptText =
        """
        val Train = throttle(1001)
        val S01 = sensor("S01")
        on { !S01 } then { Train.forward(1.speed) }
        on {  S01 } then { eStop() }
        """.trimIndent()
        )
        assertResultNoError()

        val train1 = conductorImpl.throttles[1001]!!
        val sensor1  = conductorImpl.sensors["S01"]!!

        assertThat(train1.speed).isEqualTo(0.speed)
        assertThat(sensor1.active).isFalse()
        assertThat(keyValue.getValue("V/\$estop-state\$")).isEqualTo("NORMAL")

        execEngine.onExecHandle()
        assertThat(train1.speed).isEqualTo(1.speed)
        assertThat(keyValue.getValue("V/\$estop-state\$")).isEqualTo("NORMAL")

        sensor1.active(true)
        execEngine.onExecHandle()
        execEngine.onExecHandle()
        assertThat(train1.speed).isEqualTo(0.speed)
        assertThat(keyValue.getValue("V/\$estop-state\$")).isEqualTo("ACTIVE")
    }

    @Test
    fun testConductorTime() {
        loadScriptFromText(scriptText =
        """
        val S01 = sensor("S01")
        val End_Of_Day_HHMM = 1650
        on { exportedVars.conductorTime == End_Of_Day_HHMM } then { S01.active(false) }
        """.trimIndent()
        )
        assertResultNoError()

        val sensor1  = conductorImpl.sensors["S01"]!!
        sensor1.active(true)

        clock.setNow(164901999) // see FakeClockModule

        execEngine.onExecHandle()
        assertThat(sensor1.active).isTrue()
        assertThat(keyValue.getValue("V/conductor-time")).isEqualTo("1649")

        clock.setNow(165001999) // see FakeClockModule
        execEngine.onExecHandle()
        assertThat(sensor1.active).isFalse()
        assertThat(keyValue.getValue("V/conductor-time")).isEqualTo("1650")
    }

    @Test
    fun testMqtt() {
        loadScriptFromText(scriptText =
        """
        mqtt.configure("/tmp/mqtt/config/json/path")
        mqtt.publish("some/topic1", "message1")
        mqtt.publish("some/topic2", "message2")
        """.trimIndent()
        )
        assertResultNoError()

        verify(mockMqttClient).configure(eq("/tmp/mqtt/config/json/path"))
        verify(mockMqttClient).publish("some/topic1", "message1")
        verify(mockMqttClient).publish("some/topic2", "message2")
        verifyNoMoreInteractions(mockMqttClient)
    }

    @Test
    fun testLog() {
        loadScriptFromText(scriptText =
        """
        log("This is a log message from the script")
        """.trimIndent()
        )
        assertResultNoError()

        assertThat(logger.string).contains("Script: This is a log message from the script")
    }

    @Test
    fun testNow() {
        loadScriptFromText(scriptText =
        """
        val _now = now()
        log("Date Time now is "  + _now)
        log("Year now is "  + _now.year)
        log("Month now is "  + _now.month)
        log("February now is "  + (_now.month == java.time.Month.FEBRUARY))
        log("Day now is "  + _now.dayOfMonth)
        log("Hour now is "  + _now.hour)
        log("Min now is "  + _now.minute)
        log("Sec now is "  + _now.second)
        """.trimIndent()
        )
        assertResultNoError()

        assertThat(logger.string)
            .contains(
                "Script: Date Time now is 1901-02-03T13:42:43\n" +
                "Script: Year now is 1901\n" +
                "Script: Month now is FEBRUARY\n" +
                "Script: February now is true\n" +
                "Script: Day now is 3\n" +
                "Script: Hour now is 13\n" +
                "Script: Min now is 42\n" +
                "Script: Sec now is 43"
            )
    }
}
