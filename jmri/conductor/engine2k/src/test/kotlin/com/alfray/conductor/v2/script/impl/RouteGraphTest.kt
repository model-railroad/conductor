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

/** Tests for [RouteGraph]. There's also some overlap coverage in [SequenceRouteTest]. */
class RouteGraphTest: ScriptTest2kBase() {

    @Inject internal lateinit var factory: Factory

    @Before
    fun setUp() {
        createComponent()
        scriptComponent.inject(this)
    }

    @Test
    fun testRouteGraph() {
        val (graph, nodes) = createGraph()
        val (n1, _) = nodes

        assertThat(graph.start).isSameInstanceAs(n1)
        assertThat(graph.toString()).isEqualTo(
            "[{B1}=>><B2>=<>{B1}]")
    }

    @Test
    fun testOutgoing_passive() {
        val (graph, nodes) = createGraph()
        val (n1, n2) = nodes

        assertThat(graph.outgoing(n1)).containsExactly(n2)
        assertThat(graph.outgoing(n2)).containsExactly(n1)
    }

    @Test
    fun testOutgoing_active() {
        val (graph, nodes) = createGraph()
        val (n1, n2) = nodes
        val b1 = n1.block as Block
        val b2 = n2.block as Block

        b1.active(true)
        b2.active(false)
        assertThat(graph.start).isSameInstanceAs(n1)
        assertThat(graph.outgoing(n1)).containsExactly(n2)

        b1.active(false)
        b2.active(true)
        assertThat(graph.outgoing(n2)).containsExactly(n1)
    }

    private fun createGraph(): Pair<RouteGraph, List<INode>> {
        val b1 = factory.createBlock("B1")
        val n1 = NodeBuilder(logger, b1).create()
        val b2 = factory.createBlock("B2")
        val n2 = NodeBuilder(logger, b2).create() as Node
        n2.reversal = true
        val edge12 = RouteEdge(from = n1, to = n2, forward = true, isBranch = false)
        val edge21 = RouteEdge(from = n2, to = n1, forward = false, isBranch = false)

        val graph = RouteGraph(
            n1,
            setOf(n1, n2),
            mapOf(
                b1 to listOf(edge12),
                b2 to listOf(edge21),
            )
        )
        return Pair(graph, listOf(n1, n2))
    }

}
