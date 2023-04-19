package com.alfray.conductor.v2.simulator

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test


class SimulThrottleTest : Simul2kTestBase() {

    private lateinit var throttle: SimulThrottle
    private val dccAddress = 123

    @Before
    fun setUp() {
        createComponent()

        throttle = jmriProvider.getThrottle(dccAddress) as SimulThrottle
    }

    @Test
    fun testThrottleProgression() {
        val graph = createGraph2()
        val (b1, b2) = graph.blocks
        val routeTimeout = 60

        assertThat(graph.toString())
            .isEqualTo("(start={B1}, blocks=[{B1}, <B2>], edges=[{B1}=>><B2>=<>{B1}])")

        simul2k.setRoute(dccAddress, routeTimeout, graph)

        assertThat(throttle.block).isEqualTo(b1)
        assertThat(throttle.graphForward).isTrue()

        simul2k.onExecStart()
        assertThat(throttle.block).isEqualTo(b1)
        assertThat(throttle.graphForward).isTrue()

        throttle.setSpeed(5)
        simul2k.onExecHandle()
        assertThat(throttle.block).isEqualTo(b1)
        assertThat(throttle.graphForward).isTrue()

        clock.add(throttle.blockMaxMS.toLong())
        simul2k.onExecHandle()
        assertThat(throttle.block).isEqualTo(b2)
        assertThat(throttle.graphForward).isFalse()

        clock.add(throttle.blockMaxMS.toLong())
        simul2k.onExecHandle()
        assertThat(throttle.block).isEqualTo(b1)
        assertThat(throttle.graphForward).isFalse()
    }

    private fun createGraph2(): SimulRouteGraph {
        val b1 = SimulRouteBlock("B1", "B1", virtual = false, reversal = false)
        val b2 = SimulRouteBlock("B2", "B2", virtual = false, reversal = true)
        val edge12 = SimulRouteEdge(from = b1, to = b2, forward = true, isBranch = false)
        val edge21 = SimulRouteEdge(from = b2, to = b1, forward = false, isBranch = false)

        val graph = SimulRouteGraph(
            b1,
            listOf(b1, b2),
            mapOf(
                b1 to listOf(edge12),
                b2 to listOf(edge21),
            )
        )

        return graph
    }
}
