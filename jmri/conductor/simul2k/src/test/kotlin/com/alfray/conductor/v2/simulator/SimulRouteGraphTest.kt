package com.alfray.conductor.v2.simulator

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test


class SimulRouteGraphTest : Simul2kTestBase() {

    @Before
    fun setUp() {
        createComponent()
    }

    @Test
    fun testSortEdgesByDirection() {
        val graph = createGraph3()
        val edges = graph.edges.flatMap { (_, v) -> v }
        val (b1, b2, b3) = graph.blocks

        // Unsorted should match the graph creation, except in from-block order.
        assertThat(edges.toString()).isEqualTo(
            """
            [SimulRouteEdge(from={B1}, to={B2}, forward=true, isBranch=false),
             SimulRouteEdge(from={B2}, to=<B3>, forward=true, isBranch=false),
             SimulRouteEdge(from={B2}, to={B1}, forward=false, isBranch=false),
             SimulRouteEdge(from=<B3>, to={B2}, forward=false, isBranch=false)]
            """.trimIndent().replace("\n", "")
        )
        assertThat(edges).containsExactly(
            SimulRouteEdge(from = b1, to = b2, forward = true, isBranch = false),
            SimulRouteEdge(from = b2, to = b3, forward = true, isBranch = false),
            SimulRouteEdge(from = b2, to = b1, forward = false, isBranch = false),
            SimulRouteEdge(from = b3, to = b2, forward = false, isBranch = false),
        ).inOrder()

        // Get only the edges outgoing from block b2
        val edges2 = graph.edges[b2]
        assertThat(edges2).containsExactly(
            SimulRouteEdge(from = b2, to = b3, forward = true, isBranch = false),
            SimulRouteEdge(from = b2, to = b1, forward = false, isBranch = false),
        ).inOrder()

        // Note: a previous implementation of whereTo() was *sorting* edges by
        // direction (instead of filtering them out, as it does now). This tests
        // validated the compareBy() behavior for that case. Keep it as documentation.

        // Sort them by forward in order true first, false second
        // Note: compareBy{} sorts booleans in order (false, true) so we need to
        // invert the test.
        val choice1 = true
        assertThat(edges2?.sortedWith(compareBy{ it.forward != choice1 } )).containsExactly(
            SimulRouteEdge(from = b2, to = b3, forward = true, isBranch = false),
            SimulRouteEdge(from = b2, to = b1, forward = false, isBranch = false),
        ).inOrder()


        // Sort them by forward in order false first, true second
        val choice2 = true
        assertThat(edges2?.sortedWith(compareBy{ it.forward != choice2 } )).containsExactly(
            SimulRouteEdge(from = b2, to = b3, forward = true, isBranch = false),
            SimulRouteEdge(from = b2, to = b1, forward = false, isBranch = false),
        ).inOrder()
    }

    @Test
    fun testSortEdgesByBranch() {
        val graph = createGraph3_branch1()
        val edges = graph.edges.flatMap { (_, v) -> v }
        val (b1, b2, b3, b4) = graph.blocks

        // Unsorted should match the graph creation, except in from-block order.
        assertThat(edges).containsExactly(
            SimulRouteEdge(from = b1, to = b2, forward = true, isBranch = false),
            SimulRouteEdge(from = b2, to = b3, forward = true, isBranch = false),
            SimulRouteEdge(from = b2, to = b1, forward = false, isBranch = false),
            SimulRouteEdge(from = b3, to = b2, forward = false, isBranch = false),
            SimulRouteEdge(from = b3, to = b4, forward = true, isBranch = true),
            SimulRouteEdge(from = b4, to = b3, forward = false, isBranch = true),
        ).inOrder()

        // Get only the edges outgoing from block b3
        val edges2 = graph.edges[b3]
        assertThat(edges2).containsExactly(
            SimulRouteEdge(from = b3, to = b2, forward = false, isBranch = false),
            SimulRouteEdge(from = b3, to = b4, forward = true, isBranch = true),
        ).inOrder()

        // Sort them by mainline-to-branch order
        assertThat(edges2?.sortedWith(compareBy{ it.isBranch } )).containsExactly(
            SimulRouteEdge(from = b3, to = b2, forward = false, isBranch = false),
            SimulRouteEdge(from = b3, to = b4, forward = true, isBranch = true),
        ).inOrder()

        // Sort them by branch-to-mainline order
        assertThat(edges2?.sortedWith(compareBy{ !it.isBranch } )).containsExactly(
            SimulRouteEdge(from = b3, to = b4, forward = true, isBranch = true),
            SimulRouteEdge(from = b3, to = b2, forward = false, isBranch = false),
        ).inOrder()
    }

    @Test
    fun testWhereto() {
        val graph = createGraph3()
        val (b1, b2, b3) = graph.blocks

        assertThat(graph.whereTo(from = b1, dirForward = true)).isEqualTo(b2)
        assertThat(graph.whereTo(from = b2, dirForward = true)).isEqualTo(b3)
        assertThat(graph.whereTo(from = b3, dirForward = false)).isEqualTo(b2)
        assertThat(graph.whereTo(from = b2, dirForward = false)).isEqualTo(b1)
        assertThat(graph.whereTo(from = b1, dirForward = false)).isEqualTo(null)
    }

    @Test
    fun testWhereto_withBranch() {
        val graph = createGraph3_branch1()
        val (b1, b2, b3, b4) = graph.blocks

        assertThat(graph.whereTo(from = b1, dirForward = true)).isEqualTo(b2)
        assertThat(graph.whereTo(from = b2, dirForward = true)).isEqualTo(b3)
        assertThat(graph.whereTo(from = b3, dirForward = false)).isEqualTo(b2)
        // overshoot case... engine stopped short of b3 and ended up in b4
        assertThat(graph.whereTo(from = b4, dirForward = false)).isEqualTo(b3)
        assertThat(graph.whereTo(from = b2, dirForward = false)).isEqualTo(b1)
        assertThat(graph.whereTo(from = b1, dirForward = false)).isEqualTo(null)
    }

    /** A simple graph with a simple A-B-A shuttle route with one reversal. */
    private fun createGraph3(): SimulRouteGraph {
        val b1 = SimulRouteBlock("B1", "B1", reversal = false)
        val b2 = SimulRouteBlock("B2", "B2", reversal = false)
        val b3 = SimulRouteBlock("B3", "B3", reversal = true)
        val edge12 = SimulRouteEdge(from = b1, to = b2, forward = true, isBranch = false)
        val edge23 = SimulRouteEdge(from = b2, to = b3, forward = true, isBranch = false)
        val edge32 = SimulRouteEdge(from = b3, to = b2, forward = false, isBranch = false)
        val edge21 = SimulRouteEdge(from = b2, to = b1, forward = false, isBranch = false)

        return SimulRouteGraph(
            b1,
            listOf(b1, b2, b3),
            mapOf(
                b1 to listOf(edge12),
                b2 to listOf(edge23, edge21),
                b3 to listOf(edge32)
            )
        )
    }

    /** A simple graph with a simple A-B-A shuttle route with one reversal,
     * and an error branch in case one overshoots block B, e.g. A-B-C-B-A.
     */
    private fun createGraph3_branch1(): SimulRouteGraph {
        val b1 = SimulRouteBlock("B1", "B1", reversal = false)
        val b2 = SimulRouteBlock("B2", "B2", reversal = false)
        val b3 = SimulRouteBlock("B3", "B3", reversal = true)
        val b4 = SimulRouteBlock("B4", "B4", reversal = true)
        val edge12 = SimulRouteEdge(from = b1, to = b2, forward = true, isBranch = false)
        val edge23 = SimulRouteEdge(from = b2, to = b3, forward = true, isBranch = false)
        val edge32 = SimulRouteEdge(from = b3, to = b2, forward = false, isBranch = false)
        val edge21 = SimulRouteEdge(from = b2, to = b1, forward = false, isBranch = false)
        val edge34 = SimulRouteEdge(from = b3, to = b4, forward = true, isBranch = true)
        val edge43 = SimulRouteEdge(from = b4, to = b3, forward = false, isBranch = true)

        return SimulRouteGraph(
            b1,
            listOf(b1, b2, b3, b4),
            mapOf(
                b1 to listOf(edge12),
                b2 to listOf(edge23, edge21),
                b3 to listOf(edge32, edge34),
                b4 to listOf(edge43)
            )
        )
    }
}
