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

package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.ScriptTest2kBase
import com.alfray.conductor.v2.script.dsl.INode
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

/** Tests for [RouteGraph]. There's also some overlap coverage in [SequenceRouteGraphTest]. */
class RouteGraphTest: ScriptTest2kBase() {

    @Inject internal lateinit var factory: Factory

    @Before
    fun setUp() {
        createComponent()
        scriptComponent.inject(this)
    }

    @Test
    fun testRouteGraph() {
        val (graph, nodes) = createTestGraph2Blocks()
        val (n1, _) = nodes

        assertThat(graph.start).isSameInstanceAs(n1)
        assertThat(graph.toString()).isEqualTo(
            "[{B1}=>><B2>=<>{B1}]")
    }

    @Test
    fun testOutgoing2_passive() {
        val (graph, nodes) = createTestGraph2Blocks()
        val (nA1, nA2, nB1) = nodes

        assertThat(graph.outgoing(nA1)).containsExactly(nA2)
        assertThat(graph.outgoing(nA2)).containsExactly(nB1)
        assertThat(graph.outgoing(nB1)).isEmpty()
    }

    @Test
    fun testForward2_passive() {
        val (graph, nodes) = createTestGraph2Blocks()
        val (nA1, nA2, nB1) = nodes

        assertThat(graph.forward(nA1)).containsExactly(nA2, nB1)
        assertThat(graph.forward(nA2)).containsExactly(nB1)
        assertThat(graph.outgoing(nB1)).isEmpty()
    }

    @Test
    fun testOutgoing4_passive() {
        val (graph, nodes) = createTestGraph4Blocks()
        val (nA1, nA2, nA3, nA4, nB3, nB2, nB1, nC3, nC2, nC1) = nodes

        assertThat(graph.outgoing(nA1)).containsExactly(nA2, nC3)
        assertThat(graph.outgoing(nA2)).containsExactly(nA3)
        assertThat(graph.outgoing(nA3)).containsExactly(nA4)
        assertThat(graph.outgoing(nA4)).containsExactly(nB3)
        assertThat(graph.outgoing(nB3)).containsExactly(nB2)
        assertThat(graph.outgoing(nB2)).containsExactly(nB1)
        assertThat(graph.outgoing(nB1)).isEmpty()
        assertThat(graph.outgoing(nC3)).containsExactly(nC2)
        assertThat(graph.outgoing(nC2)).containsExactly(nC1)
        assertThat(graph.outgoing(nC1)).isEmpty()
    }

    @Test
    fun testForward4_passive() {
        val (graph, nodes) = createTestGraph4Blocks()
        val (nA1, nA2, nA3, nA4, nB3, nB2, nB1, nC3, nC2, nC1) = nodes

        assertThat(graph.forward(nA1)).containsExactly(nA2, nA3, nA4, nB3, nB2, nB1, nC3, nC2, nC1)
        assertThat(graph.forward(nA2)).containsExactly(nA3, nA4, nB3, nB2, nB1)
        assertThat(graph.forward(nA3)).containsExactly(nA4, nB3, nB2, nB1)
        assertThat(graph.forward(nA4)).containsExactly(nB3, nB2, nB1)
        assertThat(graph.forward(nB3)).containsExactly(nB2, nB1)
        assertThat(graph.forward(nB2)).containsExactly(nB1)
        assertThat(graph.forward(nB1)).isEmpty()
        assertThat(graph.forward(nC3)).containsExactly(nC2, nC1)
        assertThat(graph.forward(nC2)).containsExactly(nC1)
        assertThat(graph.forward(nC1)).isEmpty()
    }

    @Test
    fun testOutgoing2_active() {
        val (graph, nodes) = createTestGraph2Blocks()
        val (nA1, nA2, nB1) = nodes
        val b1 = nA1.block as Block
        val b2 = nA2.block as Block

        b1.internalActive(true)
        b2.internalActive(false)
        assertThat(graph.start).isSameInstanceAs(nA1)
        assertThat(graph.outgoing(nA1)).containsExactly(nA2)

        b1.internalActive(false)
        b2.internalActive(true)
        assertThat(graph.outgoing(nA2)).containsExactly(nB1)
    }

    /**
     * Graph:
     * nA1   nA2   nB1
     * B1 -> B2 -> B1
     *       ^reversal
     */
    private fun createTestGraph2Blocks(): Pair<RouteGraph, List<INode>> {
        val b1 = factory.createBlock("B1")
        val nA1 = NodeBuilder(logger, b1).create()
        val nB1 = NodeBuilder(logger, b1).create()
        val b2 = factory.createBlock("B2")
        val nA2 = NodeBuilder(logger, b2).create() as Node
        nA2.reversal = true

        val graph = RouteGraph(
            start = nA1,
            nodes = setOf(nA1, nA2, nB1),
            edges = mapOf(
                b1 to listOf(RouteEdge(from = nA1, to = nA2, forward = true, isBranch = false)),
                b2 to listOf(RouteEdge(from = nA2, to = nB1, forward = false, isBranch = false)),
            )
        )
        return Pair(graph, listOf(nA1, nA2, nB1))
    }

    /**
     * Graph:
     *
     * nA1   nA2   nA3   nA4   nB3   nB2   nB1
     * B1 -> B2 -> B3 -> B4 -> B3 -> B2 -> B1
     *                   ^reversal
     * nA1   nC3   nC2   nC1
     * B1 -> B3 -> B2 -> B1
     *       ^reversal
     *
     */
    private fun createTestGraph4Blocks(): Pair<RouteGraph, List<INode>> {
        val b1 = factory.createBlock("B1")
        val nA1 = NodeBuilder(logger, b1).create()
        val nB1 = NodeBuilder(logger, b1).create()
        val nC1 = NodeBuilder(logger, b1).create()
        val b2 = factory.createBlock("B2")
        val nA2 = NodeBuilder(logger, b2).create() as Node
        val nB2 = NodeBuilder(logger, b2).create() as Node
        val nC2 = NodeBuilder(logger, b2).create() as Node
        val b3 = factory.createBlock("B3")
        val nA3 = NodeBuilder(logger, b3).create() as Node
        val nB3 = NodeBuilder(logger, b3).create() as Node
        val nC3 = NodeBuilder(logger, b3).create() as Node
        val b4 = factory.createBlock("B4")
        val nA4 = NodeBuilder(logger, b4).create() as Node
        nA4.reversal = true
        nC3.reversal = true
        val graph = RouteGraph(
            start = nA1,
            nodes = setOf(nA1, nA2, nA3, nA4, nB3, nB2, nB1, nC3, nC2, nC1),
            edges = mapOf(
                b1 to listOf(
                    RouteEdge(from = nA1, to = nA2, forward = true, isBranch = false),
                    RouteEdge(from = nA1, to = nC3, forward = true, isBranch = true)),
                b2 to listOf(
                    RouteEdge(from = nA2, to = nA3, forward = true, isBranch = false),
                    RouteEdge(from = nB2, to = nB1, forward = false, isBranch = false),
                    RouteEdge(from = nC2, to = nC1, forward = false, isBranch = true)),
                b3 to listOf(
                    RouteEdge(from = nA3, to = nA4, forward = true, isBranch = false),
                    RouteEdge(from = nB3, to = nB2, forward = false, isBranch = false),
                    RouteEdge(from = nC3, to = nC2, forward = false, isBranch = true)),
                b4 to listOf(
                    RouteEdge(from = nA4, to = nB3, forward = false, isBranch = false)),
            )
        )
        return Pair(graph, listOf(nA1, nA2, nA3, nA4, nB3, nB2, nB1, nC3, nC2, nC1))
    }

}

private operator fun <E> List<E>.component6(): E = this[5]
private operator fun <E> List<E>.component7(): E = this[6]
private operator fun <E> List<E>.component8(): E = this[7]
private operator fun <E> List<E>.component9(): E = this[8]
private operator fun <E> List<E>.component10(): E = this[9]
