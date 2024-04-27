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
import com.alfray.conductor.v2.script.dsl.IBlock
import com.alfray.conductor.v2.script.dsl.speed
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

/** Tests the sequence management side of the [SequenceRoute]. */
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
            minSecondsOnBlock = 0
            maxSecondsEnterBlock = 0
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
        assertThat(conductorImpl.routesContainers).hasSize(1)

        val route = conductorImpl.routesContainers[0].active as SequenceRoute
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVATED)
        execEngine.onExecHandle()
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)

        // B1 occupied
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("<B1> OCCUPIED, {V2} EMPTY, {B3} EMPTY, {B4} EMPTY")
        assertThat(route.throttle.speed).isEqualTo(5.speed)

        // B1 -> V2 transition
        jmriProvider.getSensor("B1").isActive = false
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("{B1} TRAILING, {V2} OCCUPIED, {B3} EMPTY, {B4} EMPTY")
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("{B1} TRAILING, <V2> OCCUPIED, {B3} EMPTY, {B4} EMPTY")

        // V2 -> B3 transition
        jmriProvider.getSensor("B3").isActive = true
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("{B1} EMPTY, <V2> TRAILING, <B3> OCCUPIED, {B4} EMPTY")

        // B3 -> B4 transition (both temporarily active)
        jmriProvider.getSensor("B3").isActive = true
        jmriProvider.getSensor("B4").isActive = true
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("{B1} EMPTY, {V2} EMPTY, <B3> TRAILING, <B4> OCCUPIED")

        // B3 -> B4 transition... train on the reversing block now.
        jmriProvider.getSensor("B3").isActive = false
        jmriProvider.getSensor("B4").isActive = true
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("{B1} EMPTY, {V2} EMPTY, {B3} TRAILING, <B4> OCCUPIED")
        assertThat(route.throttle.speed).isEqualTo((-5).speed)

        // B4 -> B3 transition (both temporarily active)
        jmriProvider.getSensor("B3").isActive = true
        jmriProvider.getSensor("B4").isActive = true
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("{B1} EMPTY, {V2} EMPTY, <B3> OCCUPIED, <B4> TRAILING")

        // B4 -> B3 transition
        jmriProvider.getSensor("B3").isActive = true
        jmriProvider.getSensor("B4").isActive = false
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("{B1} EMPTY, {V2} EMPTY, <B3> OCCUPIED, {B4} TRAILING")

        // B3 -> V2 transition
        jmriProvider.getSensor("B3").isActive = false
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("{B1} EMPTY, {V2} OCCUPIED, {B3} TRAILING, {B4} EMPTY")
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("{B1} EMPTY, <V2> OCCUPIED, {B3} TRAILING, {B4} EMPTY")

        // V2 -> B1 transition
        jmriProvider.getSensor("B1").isActive = true
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("<B1> OCCUPIED, <V2> TRAILING, {B3} EMPTY, {B4} EMPTY")
        execEngine.onExecHandle()
        assertThat(route.print()).isEqualTo("<B1> OCCUPIED, {V2} TRAILING, {B3} EMPTY, {B4} EMPTY")
        assertThat(route.throttle.speed).isEqualTo(0.speed)

        assertThat(logger.string).doesNotContain("ERROR")
    }

    @Test
    fun minSecondsOnBlock_withRouteValueOnly_activateNextBlockAfterMinimum() {
        jmriProvider.getSensor("B1").isActive = true
        loadScriptFromText(scriptText =
        """
        val T1 = throttle(1001)
        val B1 = block("B1")
        val B2 = block("B2")
        val Toggle = sensor("S1")
        val Routes = routes {
            name = "PA"
            toggle = Toggle
        }
        val Route_Seq = Routes.sequence {
            throttle = T1
            minSecondsOnBlock = 42
            val b1_fwd = node(B1) { onEnter { T1.forward(5.speed) } }
            val b2_fwd = node(B2) { onEnter { T1.reverse(5.speed) } }
            sequence = listOf(b1_fwd, b2_fwd)
        }
        """.trimIndent()
        )
        assertResultNoError()
        assertThat(conductorImpl.routesContainers).hasSize(1)

        val route = conductorImpl.routesContainers[0].active as SequenceRoute
        val block1 = conductorImpl.blocks["B1"] as Block
        val block2 = conductorImpl.blocks["B2"] as Block
        val throttle = route.throttle
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVATED)
        execEngine.onExecHandle()
        execEngine.onExecHandle()
        execEngine.onExecHandle()
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)
        assertThat(block1.occupied).isTrue()
        assertThat(throttle.speed).isEqualTo(5.speed)

        // Activate next block not too early
        clock.add(43 * 1000)
        jmriProvider.getSensor("B1").isActive = true
        jmriProvider.getSensor("B2").isActive = true
        execEngine.onExecHandle()
        assertThat(block1.state).isEqualTo(IBlock.State.TRAILING)
        assertThat(block2.occupied).isTrue()
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)

        assertThat(logger.string).doesNotContain("ERROR")
    }

    @Test
    fun minSecondsOnBlock_withRouteValueOnly_activateNextBlockTooEarly() {
        jmriProvider.getSensor("B1").isActive = true
        loadScriptFromText(scriptText =
        """
        val T1 = throttle(1001)
        val B1 = block("B1")
        val B2 = block("B2")
        val Toggle = sensor("S1")
        val Routes = routes {
            name = "PA"
            toggle = Toggle
        }
        val Route_Seq = Routes.sequence {
            throttle = T1
            minSecondsOnBlock = 42
            val b1_fwd = node(B1) { onEnter { T1.forward(5.speed) } }
            val b2_fwd = node(B2) { onEnter { T1.reverse(5.speed) } }
            sequence = listOf(b1_fwd, b2_fwd)
        }
        """.trimIndent()
        )
        assertResultNoError()
        assertThat(conductorImpl.routesContainers).hasSize(1)

        val route = conductorImpl.routesContainers[0].active as SequenceRoute
        val block1 = conductorImpl.blocks["B1"] as Block
        val block2 = conductorImpl.blocks["B2"] as Block
        val throttle = route.throttle
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVATED)
        execEngine.onExecHandle()
        execEngine.onExecHandle()
        execEngine.onExecHandle()
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)
        assertThat(block1.occupied).isTrue()
        assertThat(throttle.speed).isEqualTo(5.speed)

        // Activate next block too early
        clock.add(40 * 1000)
        jmriProvider.getSensor("B1").isActive = true
        jmriProvider.getSensor("B2").isActive = true
        execEngine.onExecHandle()
        assertThat(block1.state).isEqualTo(IBlock.State.TRAILING)
        assertThat(block2.occupied).isTrue()
        assertThat(route.state).isEqualTo(RouteBase.State.ERROR)

        assertThat(logger.string).contains("ERROR Sequence PA #0 (1001) next block <B2> activated in 40.1 seconds. Current block <B1> must remain occupied for at least 42 seconds")
    }

    @Test
    fun minSecondsOnBlock_withNodeOverride_activateNextBlockTooEarly() {
        jmriProvider.getSensor("B1").isActive = true
        loadScriptFromText(scriptText =
        """
        val T1 = throttle(1001)
        val B1 = block("B1")
        val B2 = block("B2")
        val Toggle = sensor("S1")
        val Routes = routes {
            name = "PA"
            toggle = Toggle
        }
        val Route_Seq = Routes.sequence {
            throttle = T1
            minSecondsOnBlock = 42
            val b1_fwd = node(B1) {
                minSecondsOnBlock = 60
                onEnter { T1.forward(5.speed) } 
            }
            val b2_fwd = node(B2) {
                minSecondsOnBlock = 120
                onEnter { T1.reverse(5.speed) }
            }
            sequence = listOf(b1_fwd, b2_fwd)
        }
        """.trimIndent()
        )
        assertResultNoError()
        assertThat(conductorImpl.routesContainers).hasSize(1)

        val route = conductorImpl.routesContainers[0].active as SequenceRoute
        val block1 = conductorImpl.blocks["B1"] as Block
        val block2 = conductorImpl.blocks["B2"] as Block
        val throttle = route.throttle
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVATED)
        execEngine.onExecHandle()
        execEngine.onExecHandle()
        execEngine.onExecHandle()
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)
        assertThat(block1.occupied).isTrue()
        assertThat(throttle.speed).isEqualTo(5.speed)

        // Activate next block too early
        clock.add(50 * 1000)
        jmriProvider.getSensor("B1").isActive = true
        jmriProvider.getSensor("B2").isActive = true
        execEngine.onExecHandle()
        assertThat(block1.state).isEqualTo(IBlock.State.TRAILING)
        assertThat(block2.occupied).isTrue()
        assertThat(route.state).isEqualTo(RouteBase.State.ERROR)

        assertThat(logger.string).contains("ERROR Sequence PA #0 (1001) next block <B2> activated in 50.1 seconds. Current block <B1> must remain occupied for at least 60 seconds")
    }

    @Test
    fun minSecondsOnBlock_withReversingBlock_activatedTooEarly() {
        jmriProvider.getSensor("B1").isActive = true
        loadScriptFromText(scriptText =
        """
        val T1 = throttle(1001)
        val B1 = block("B1")
        val B2 = block("B2")
        val Toggle = sensor("S1")
        val Routes = routes {
            name = "PA"
            toggle = Toggle
        }
        val Route_Seq = Routes.sequence {
            throttle = T1
            minSecondsOnBlock = 42
            val b1_fwd = node(B1) { onEnter { T1.forward(5.speed) } }
            val b2_end = node(B2) { onEnter { after (20.seconds) then { T1.reverse(6.speed) } } }
            val b1_rev = node(B1) { onEnter { T1.reverse(7.speed) } }
            sequence = listOf(b1_fwd, b2_end, b1_rev)
        }
        """.trimIndent()
        )
        assertResultNoError()
        assertThat(conductorImpl.routesContainers).hasSize(1)

        val route = conductorImpl.routesContainers[0].active as SequenceRoute
        val block1 = conductorImpl.blocks["B1"] as Block
        val block2 = conductorImpl.blocks["B2"] as Block
        val throttle = route.throttle
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVATED)
        execEngine.onExecHandle()
        execEngine.onExecHandle()
        execEngine.onExecHandle()
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)
        assertThat(block1.occupied).isTrue()
        assertThat(throttle.speed).isEqualTo(5.speed)

        // Activate next block B2 not too early
        clock.add(45 * 1000)
        jmriProvider.getSensor("B1").isActive = false
        jmriProvider.getSensor("B2").isActive = true
        execEngine.onExecHandle()
        assertThat(block1.state).isEqualTo(IBlock.State.TRAILING)
        assertThat(block2.occupied).isTrue()
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)

        // While we enter B2, train wheels bridge rails gap and prematurely activate B1
        clock.add(5 * 1000)
        jmriProvider.getSensor("B1").isActive = true
        jmriProvider.getSensor("B2").isActive = true
        execEngine.onExecHandle()
        assertThat(block1.state).isEqualTo(IBlock.State.TRAILING)
        assertThat(block2.occupied).isTrue()
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)
        assertThat(logger.string).contains("WARNING ignore trailing block {B1} activated after 5.0 seconds")
        assertThat(throttle.speed).isEqualTo(5.speed)

        // Train continues on B2 till it reverses
        clock.add(20 * 1000)
        jmriProvider.getSensor("B1").isActive = false
        jmriProvider.getSensor("B2").isActive = true
        execEngine.onExecHandle()
        assertThat(block1.state).isEqualTo(IBlock.State.TRAILING)
        assertThat(block2.occupied).isTrue()
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)
        assertThat(throttle.speed).isEqualTo(6.speed.reverse())

        // And back to B1
        clock.add(20 * 1000)
        jmriProvider.getSensor("B1").isActive = true
        jmriProvider.getSensor("B2").isActive = false
        execEngine.onExecHandle()
        assertThat(block1.occupied).isTrue()
        assertThat(block2.state).isEqualTo(IBlock.State.TRAILING)
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)
        assertThat(throttle.speed).isEqualTo(7.speed.reverse())

        // Temporary activation of B2 as the train crosses rails gap is ignored
        clock.add(1 * 1000)
        jmriProvider.getSensor("B1").isActive = true
        jmriProvider.getSensor("B2").isActive = true
        execEngine.onExecHandle()
        assertThat(block1.occupied).isTrue()
        assertThat(block2.state).isEqualTo(IBlock.State.TRAILING)
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)
        assertThat(throttle.speed).isEqualTo(7.speed.reverse())

        assertThat(logger.string).doesNotContain("ERROR")
    }

    @Test
    fun maxSecondsOnBlock_withRouteValueOnly() {
        jmriProvider.getSensor("B1").isActive = true
        loadScriptFromText(scriptText =
        """
        val T1 = throttle(1001)
        val B1 = block("B1")
        val B2 = block("B2")
        val Toggle = sensor("S1")
        val Routes = routes {
            name = "PA"
            toggle = Toggle
        }
        val Route_Seq = Routes.sequence {
            throttle = T1
            maxSecondsOnBlock = 42
            val b1_fwd = node(B1) { onEnter { T1.forward(5.speed) } }
            val b2_fwd = node(B2) { onEnter { T1.reverse(5.speed) } }
            sequence = listOf(b1_fwd, b2_fwd)
        }
        """.trimIndent()
        )
        assertResultNoError()
        assertThat(conductorImpl.routesContainers).hasSize(1)

        val route = conductorImpl.routesContainers[0].active as SequenceRoute
        val block1 = conductorImpl.blocks["B1"] as Block
        val throttle = route.throttle
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVATED)
        execEngine.onExecHandle()
        execEngine.onExecHandle()
        execEngine.onExecHandle()
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)
        assertThat(block1.occupied).isTrue()
        assertThat(throttle.speed).isEqualTo(5.speed)

        // Advance by 41 seconds... this is still under the maxSecondsOnBlock timeout of 42 seconds.
        clock.add(41 * 1000)
        execEngine.onExecHandle()
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)

        // Advance by 1 second... this is just over the maxSecondsOnBlock timeout of 42 seconds.
        clock.add(1 * 1000)
        execEngine.onExecHandle()
        assertThat(route.state).isEqualTo(RouteBase.State.ERROR)

        assertThat(logger.string).contains("ERROR Sequence PA #0 (1001) current block <B1> still occupied after 42.1 seconds")
    }

    @Test
    fun maxSecondsOnBlock_withNodeOverride() {
        jmriProvider.getSensor("B1").isActive = true
        loadScriptFromText(scriptText =
        """
        val T1 = throttle(1001)
        val B1 = block("B1")
        val B2 = block("B2")
        val Toggle = sensor("S1")
        val Routes = routes {
            name = "PA"
            toggle = Toggle
        }
        val Route_Seq = Routes.sequence {
            throttle = T1
            maxSecondsOnBlock = 42
            val b1_fwd = node(B1) { 
                maxSecondsOnBlock = 53
                onEnter { T1.forward(5.speed) }
            }
            val b2_fwd = node(B2) { onEnter { T1.reverse(5.speed) } }
            sequence = listOf(b1_fwd, b2_fwd)
        }
        """.trimIndent()
        )
        assertResultNoError()
        assertThat(conductorImpl.routesContainers).hasSize(1)

        val route = conductorImpl.routesContainers[0].active as SequenceRoute
        val block1 = conductorImpl.blocks["B1"] as Block
        val throttle = route.throttle
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVATED)
        execEngine.onExecHandle()
        execEngine.onExecHandle()
        execEngine.onExecHandle()
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)
        assertThat(block1.occupied).isTrue()
        assertThat(throttle.speed).isEqualTo(5.speed)

        // Advance by 41 seconds... this is still under the maxSecondsOnBlock timeout of 42 seconds.
        clock.add(41 * 1000)
        execEngine.onExecHandle()
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)

        // Advance by 1 second... this is just over the maxSecondsOnBlock timeout of 42 seconds,
        // and it's not an error since the node for B1 overrides maxSecondsOnBlock to 53.
        clock.add(1 * 1000)
        execEngine.onExecHandle()
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)

        // Advance by 53 second... this is just over the maxSecondsOnBlock timeout of 42 seconds,
        // and it's not an error since the node for B1 overrides maxSecondsOnBlock to 53.
        clock.add((53 - 42) * 1000)
        execEngine.onExecHandle()
        assertThat(route.state).isEqualTo(RouteBase.State.ERROR)

        assertThat(logger.string).contains("ERROR Sequence PA #0 (1001) current block <B1> still occupied after 53.1 seconds")
    }

    @Test
    fun recovery_fromNonOverlappingBlocks() {
        jmriProvider.getSensor("B01").isActive = false
        jmriProvider.getSensor("B02").isActive = true
        jmriProvider.getSensor("B03").isActive = false
        jmriProvider.getSensor("B04").isActive = false
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
        var n = 1
        val Recovery_Seq = Routes.sequence {
            throttle = Train1
            val block1_fwd = node(Block1) { onEnter { Train1.reverse(1.speed) } }
            val block2_fwd = node(Block2) { onEnter { Train1.reverse(2.speed) } }
            val block3_fwd = node(Block3) { onEnter { Train1.reverse(3.speed) } }
            val block4_fwd = node(Block4) { onEnter { Train1.reverse(4.speed) } }
            onActivate {
                if      (Block4.active) { route.startNode(block4_fwd) }
                else if (Block3.active) { route.startNode(block3_fwd) }
                else if (Block2.active) { route.startNode(block2_fwd) }
                else if (Block1.active) { /* no-op as block1_fwd is the default */ }
            }
            // in a recovery blocks are listed in reverse order
            sequence = listOf(block4_fwd, block3_fwd, block2_fwd, block1_fwd)
        }
        """.trimIndent()
        )
        assertResultNoError()
        assertThat(conductorImpl.routesContainers).hasSize(1)

        val t1 = conductorImpl.throttles[1001]!!
        val block1 = conductorImpl.blocks["B01"]!!
        val block2 = conductorImpl.blocks["B02"]!!
        val block3 = conductorImpl.blocks["B03"]!!
        val block4 = conductorImpl.blocks["B04"]!!
        val route = conductorImpl.routesContainers[0].active as RouteBase

        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVATED)
        assertThat(t1.speed).isEqualTo(0.speed)

        execEngine.onExecHandle()
        execEngine.onExecHandle()
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)
        assertThat(block1.state).isEqualTo(IBlock.State.EMPTY)
        assertThat(block2.state).isEqualTo(IBlock.State.OCCUPIED)
        assertThat(block3.state).isEqualTo(IBlock.State.EMPTY)
        assertThat(block4.state).isEqualTo(IBlock.State.EMPTY)
        assertThat(t1.speed).isEqualTo(2.speed.reverse())
    }

    @Test
    fun recovery_fromOverlappingBlocks_broken() {
        /*
            This is my initial attempty at doing route recovery. It works only when a single
            block is occupied. It fails if the train overlaps 2 blocks. Issues here:
            - We end up with a graph where multiple blocks are OCCUPIED, whereas ideally we would
              want one block OCCUPIED and one TRAILING in the _proper_ direction of the recovery.
            - This still somewhat works because we check blocks in reverse older, thus we end up
              with B03 current followed by B02 active... this matches the route order "as if" a
              train was just crossing a boundary. However since the route starts in that
              configuration, it never transitions the OCCUPIED+OCCUPIED state to TRAILING+OCCUPIED.
            - This is highly dependent on the order checks are done in the onActivate method.
         */
        jmriProvider.getSensor("B01").isActive = false
        jmriProvider.getSensor("B02").isActive = true
        jmriProvider.getSensor("B03").isActive = true
        jmriProvider.getSensor("B04").isActive = false
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
        var n = 1
        val Recovery_Seq = Routes.sequence {
            throttle = Train1
            val block1_fwd = node(Block1) { onEnter { Train1.reverse(1.speed) } }
            val block2_fwd = node(Block2) { onEnter { Train1.reverse(2.speed) } }
            val block3_fwd = node(Block3) { onEnter { Train1.reverse(3.speed) } }
            val block4_fwd = node(Block4) { onEnter { Train1.reverse(4.speed) } }
            onActivate {
                if      (Block4.active) { route.startNode(block4_fwd) }
                else if (Block3.active) { route.startNode(block3_fwd) }
                else if (Block2.active) { route.startNode(block2_fwd) }
                else if (Block1.active) { /* no-op as block1_fwd is the default */ }
            }
            // in a recovery blocks are listed in reverse order
            sequence = listOf(block4_fwd, block3_fwd, block2_fwd, block1_fwd)
        }
        """.trimIndent()
        )
        assertResultNoError()
        assertThat(conductorImpl.routesContainers).hasSize(1)

        val t1 = conductorImpl.throttles[1001]!!
        val block1 = conductorImpl.blocks["B01"]!!
        val block2 = conductorImpl.blocks["B02"]!!
        val block3 = conductorImpl.blocks["B03"]!!
        val block4 = conductorImpl.blocks["B04"]!!
        val route = conductorImpl.routesContainers[0].active as RouteBase

        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVATED)
        assertThat(t1.speed).isEqualTo(0.speed)

        execEngine.onExecHandle()
        execEngine.onExecHandle()
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)
        assertThat(block1.state).isEqualTo(IBlock.State.EMPTY)
        assertThat(block2.state).isEqualTo(IBlock.State.OCCUPIED)
        assertThat(block3.state).isEqualTo(IBlock.State.OCCUPIED)
        assertThat(block4.state).isEqualTo(IBlock.State.EMPTY)
        assertThat(t1.speed).isEqualTo(3.speed.reverse())
    }

    @Test
    fun recovery_fromOverlappingBlocks_fixed() {
        jmriProvider.getSensor("B01").isActive = false
        jmriProvider.getSensor("B02").isActive = true
        jmriProvider.getSensor("B03").isActive = true
        jmriProvider.getSensor("B04").isActive = false
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
        var n = 1
        val Recovery_Seq = Routes.sequence {
            throttle = Train1
            val block1_fwd = node(Block1) { onEnter { Train1.reverse(1.speed) } }
            val block2_fwd = node(Block2) { onEnter { Train1.reverse(2.speed) } }
            val block3_fwd = node(Block3) { onEnter { Train1.reverse(3.speed) } }
            val block4_fwd = node(Block4) { onEnter { Train1.reverse(4.speed) } }
            onActivate {
                if      (Block4.active && Block3.active) { route.startNode(block3_fwd, trailing=block4_fwd) }
                else if (Block4.active)                  { route.startNode(block4_fwd) }
                else if (Block3.active && Block2.active) { route.startNode(block2_fwd, trailing=block3_fwd) }
                else if (Block3.active)                  { route.startNode(block3_fwd) }
                else if (Block2.active && Block1.active) { route.startNode(block1_fwd, trailing=block2_fwd) }
                else if (Block2.active)                  { route.startNode(block2_fwd) }
                else if (Block1.active)                  { /* no-op as block1_fwd is the default */ }
            }
            // in a recovery blocks are listed in reverse order
            sequence = listOf(block4_fwd, block3_fwd, block2_fwd, block1_fwd)
        }
        """.trimIndent()
        )
        assertResultNoError()
        assertThat(conductorImpl.routesContainers).hasSize(1)

        val t1 = conductorImpl.throttles[1001]!!
        val block1 = conductorImpl.blocks["B01"]!!
        val block2 = conductorImpl.blocks["B02"]!!
        val block3 = conductorImpl.blocks["B03"]!!
        val block4 = conductorImpl.blocks["B04"]!!
        val route = conductorImpl.routesContainers[0].active as RouteBase

        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVATED)
        assertThat(t1.speed).isEqualTo(0.speed)

        execEngine.onExecHandle()
        execEngine.onExecHandle()
        assertThat(route.state).isEqualTo(RouteBase.State.ACTIVE)
        assertThat(block1.state).isEqualTo(IBlock.State.EMPTY)
        assertThat(block2.state).isEqualTo(IBlock.State.OCCUPIED)
        assertThat(block3.state).isEqualTo(IBlock.State.TRAILING)
        assertThat(block4.state).isEqualTo(IBlock.State.EMPTY)
        assertThat(t1.speed).isEqualTo(2.speed.reverse())
    }

    @Test
    fun testSequenceRoute_activateTwice() {
        // This case tests a "shuttle" implemented using 2 separate routes.
        jmriProvider.getSensor("B1").isActive = true
        loadScriptFromText(scriptText =
        """
        val T1 = throttle(1001)
        val B1 = block("B1")
        val B2 = block("B2")
        val Toggle = sensor("S1")
        val S2 = sensor("S2")
        val Routes = routes {
            name = "PA"
            toggle = Toggle
        }
        var nextRoute: IRoute? = null
        lateinit var RouteSeqB: IRoute
        val RouteSeqA = Routes.sequence {
            throttle = T1
            minSecondsOnBlock = 0
            maxSecondsEnterBlock = 0
            val b1_fwd = node(B1) { onEnter { T1.forward(5.speed) } }
            val b2_fwd = node(B2) { onEnter { 
                T1.stop()
                nextRoute = RouteSeqB
            } }
            sequence = listOf(b1_fwd, b2_fwd)
        }
        RouteSeqB = Routes.sequence {
            throttle = T1
            minSecondsOnBlock = 0
            maxSecondsEnterBlock = 0
            val b2_rev = node(B2) { onEnter { T1.reverse(5.speed) } }
            val b1_rev = node(B1) { onEnter { 
                T1.stop()
                nextRoute = RouteSeqA
            } }
            sequence = listOf(b2_rev, b1_rev)
        }
        on { S2.active && nextRoute != null } then { nextRoute!!.activate() }
        """.trimIndent()
        )
        assertResultNoError()
        assertThat(conductorImpl.routesContainers).hasSize(1)

        val s2 = conductorImpl.sensors["S2"]!!

        val ar = conductorImpl.routesContainers[0] as RoutesContainer
        assertThat(ar.routes).hasSize(2)
        val routeA = ar.routes[0] as SequenceRoute
        val routeB = ar.routes[1] as SequenceRoute

        // <Setup> Run the RouteA once, from B1 and B2.
        assertThat(routeA.state).isEqualTo(RouteBase.State.ACTIVATED)
        assertThat(routeB.state).isEqualTo(RouteBase.State.IDLE)
        execEngine.onExecHandle()
        assertThat(routeA.state).isEqualTo(RouteBase.State.ACTIVE)
        // RouteA's initial node is B1
        assertThat(routeA.currentNode.toString()).isEqualTo("{B1}")
        // B1 occupied
        execEngine.onExecHandle()
        assertThat(routeA.print()).isEqualTo("<B1> OCCUPIED, {B2} EMPTY")
        assertThat(routeA.throttle.speed).isEqualTo(5.speed)
        // B1 -> B2 transition
        jmriProvider.getSensor("B1").isActive = false
        jmriProvider.getSensor("B2").isActive = true
        execEngine.onExecHandle()
        assertThat(routeA.print()).isEqualTo("{B1} TRAILING, <B2> OCCUPIED")
        assertThat(routeA.throttle.speed).isEqualTo(0.speed)
        // And the end of the run, the route's current node is the last occupied node.
        assertThat(routeA.currentNode.toString()).isEqualTo("{B2}")

        // <Setup> Run the RouteB, from B2 to B1.
        s2.active(true)
        execEngine.onExecHandle()
        s2.active(false)
        assertThat(routeA.state).isEqualTo(RouteBase.State.IDLE)
        assertThat(routeB.state).isEqualTo(RouteBase.State.ACTIVATED)
        execEngine.onExecHandle()
        assertThat(routeB.state).isEqualTo(RouteBase.State.ACTIVE)
        // RouteB's initial node is B1
        assertThat(routeB.currentNode.toString()).isEqualTo("{B2}")
        // B2 occupied
        execEngine.onExecHandle()
        assertThat(routeB.print()).isEqualTo("<B2> OCCUPIED, {B1} EMPTY")
        assertThat(routeB.throttle.speed).isEqualTo((-5).speed)
        // B2 -> B1 transition
        jmriProvider.getSensor("B2").isActive = false
        jmriProvider.getSensor("B1").isActive = true
        execEngine.onExecHandle()
        assertThat(routeB.print()).isEqualTo("{B2} TRAILING, <B1> OCCUPIED")
        assertThat(routeB.throttle.speed).isEqualTo(0.speed)
        // And the end of the run, the route's current node is the last occupied node.
        assertThat(routeB.currentNode.toString()).isEqualTo("{B1}")

        // Now try to re-run RouteA again.
        // Since RouteA's has already be run, its currentNode is still {B2}. whereas the engine has
        // shuttled back to its original {B1} position. The currentNode must be reset when the route
        // is (re)activated, in order to reinitialize it to its default or override it in the
        // onActivated callback.
        s2.active(true)
        execEngine.onExecHandle()
        s2.active(false)
        assertThat(routeA.state).isEqualTo(RouteBase.State.ACTIVATED)
        assertThat(routeB.state).isEqualTo(RouteBase.State.IDLE)
        execEngine.onExecHandle()
        assertThat(routeA.state).isEqualTo(RouteBase.State.ACTIVE)
        // RouteA's initial node is B1
        assertThat(routeA.currentNode.toString()).isEqualTo("{B1}")
        // B1 occupied
        execEngine.onExecHandle()
        assertThat(routeA.print()).isEqualTo("<B1> OCCUPIED, {B2} EMPTY")
        assertThat(routeA.throttle.speed).isEqualTo(5.speed)
        // B1 -> B2 transition
        jmriProvider.getSensor("B1").isActive = false
        jmriProvider.getSensor("B2").isActive = true
        execEngine.onExecHandle()
        assertThat(routeA.print()).isEqualTo("{B1} TRAILING, <B2> OCCUPIED")
        assertThat(routeA.throttle.speed).isEqualTo(0.speed)
        // And the end of the run, the route's current node is the last occupied node.
        assertThat(routeA.currentNode.toString()).isEqualTo("{B2}")

        assertThat(logger.string).doesNotContain("ERROR")
    }

    private fun SequenceRoute.print() =
        this.graph.blocks.joinToString { "$it ${it.state}" }
}
