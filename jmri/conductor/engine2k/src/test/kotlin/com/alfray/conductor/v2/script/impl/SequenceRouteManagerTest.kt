/*
 * Project: Conductor
 * Copyright (C) 2023 alf.labs gmail com,
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

package com.alfray.conductor.v2.script.impl

import com.alflabs.kv.IKeyValue
import com.alflabs.utils.FakeClock
import com.alfray.conductor.v2.script.CurrentContext
import com.alfray.conductor.v2.script.ScriptTest2kBase
import com.alfray.conductor.v2.script.dsl.speed
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

/** Tests most features of the Conductor 2 DSL scripting engine. */
class SequenceRouteManagerTest : ScriptTest2kBase() {
    @Inject lateinit var clock: FakeClock
    @Inject lateinit var keyValue: IKeyValue
    @Inject internal lateinit var currentContext: CurrentContext

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
    fun testSequenceRoute_start_virtualBlockForward() {
        jmriProvider.getSensor("B1").isActive = true
        loadScriptFromText(scriptText =
        """
        val T1 = throttle(1001)
        val B1 = block("B1")
        val V2 = virtualBlock("V2")
        val B3 = block("B3")
        val B4 = block("B4")
        val Toggle = sensor("S1")
        val Routes = routes {
            name = "PA"
            toggle = Toggle
        }
        val Route_Seq = Routes.sequence {
            throttle = T1
            val b1_fwd = node(B1) { onEnter { T1.forward(5.speed) } }
            val v2_fwd = node(V2) {}
            val b3_fwd = node(B3) {}
            val b4_end = node(B4) { onEnter { T1.reverse(5.speed) } }
            val b3_rev = node(B3) {}
            val v2_rev = node(V2) {}
            val b1_rev = node(B1) { onEnter { T1.stop() } }
            sequence = listOf(b1_fwd, v2_fwd, b3_fwd, b4_end, b3_rev, v2_rev, b1_rev)
        }
        """.trimIndent()
        )
        assertResultNoError()
        assertThat(conductorImpl.rules).hasSize(0)
        assertThat(conductorImpl.routesContainers).hasSize(1)

        val route = conductorImpl.routesContainers[0].active as SequenceRoute
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVATED)
        execEngine.onExecHandle()
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)

        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("<B1> OCCUPIED, {V2} EMPTY, {B3} EMPTY, {B4} EMPTY")
        assertThat(route.throttle.speed).isEqualTo(5.speed)

        jmriProvider.getSensor("B1").isActive = false
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("{B1} TRAILING, {V2} OCCUPIED, {B3} EMPTY, {B4} EMPTY")
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("{B1} TRAILING, <V2> OCCUPIED, {B3} EMPTY, {B4} EMPTY")

        jmriProvider.getSensor("B3").isActive = true
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("{B1} EMPTY, <V2> TRAILING, <B3> OCCUPIED, {B4} EMPTY")

        jmriProvider.getSensor("B3").isActive = true
        jmriProvider.getSensor("B4").isActive = true
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("{B1} EMPTY, {V2} EMPTY, <B3> TRAILING, <B4> OCCUPIED")
        assertThat(route.throttle.speed).isEqualTo((-5).speed)

        jmriProvider.getSensor("B3").isActive = false
        jmriProvider.getSensor("B4").isActive = true
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("{B1} EMPTY, {V2} EMPTY, {B3} TRAILING, <B4> OCCUPIED")

        jmriProvider.getSensor("B3").isActive = true
        jmriProvider.getSensor("B4").isActive = true
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("{B1} EMPTY, {V2} EMPTY, <B3> OCCUPIED, <B4> TRAILING")

        jmriProvider.getSensor("B3").isActive = true
        jmriProvider.getSensor("B4").isActive = false
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("{B1} EMPTY, {V2} EMPTY, <B3> OCCUPIED, {B4} TRAILING")

        jmriProvider.getSensor("B3").isActive = false
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("{B1} EMPTY, {V2} OCCUPIED, {B3} TRAILING, {B4} EMPTY")
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("{B1} EMPTY, <V2> OCCUPIED, {B3} TRAILING, {B4} EMPTY")

        jmriProvider.getSensor("B1").isActive = true
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("<B1> OCCUPIED, <V2> TRAILING, {B3} EMPTY, {B4} EMPTY")
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("<B1> OCCUPIED, {V2} TRAILING, {B3} EMPTY, {B4} EMPTY")
        assertThat(route.throttle.speed).isEqualTo(0.speed)

        assertThat(logger.string).doesNotContain("ERROR")
    }

    private fun SequenceRoute.print() =
        this.graph.blocks.joinToString { "$it ${it.state}" }
}
