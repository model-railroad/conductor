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

import com.alflabs.conductor.jmri.FakeJmriProvider
import com.alflabs.conductor.util.EventLogger
import com.alflabs.kv.IKeyValue
import com.alfray.conductor.v2.script.CondCache
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import dagger.internal.InstanceFactory
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.lang.IllegalStateException

class RouteSequenceTest {
    private lateinit var blockFactory: Block_Factory
    private val jmriProvider = FakeJmriProvider()
    private val eventLogger = mock<EventLogger>()
    private val keyValue = mock<IKeyValue>()
    private val condCache = CondCache()

    @Before
    fun setUp() {
        blockFactory = Block_Factory(
            InstanceFactory.create(keyValue),
            InstanceFactory.create(condCache),
            InstanceFactory.create(eventLogger),
            InstanceFactory.create(jmriProvider))
    }

    @Test
    fun testSequenceLinearGraph0_throwsWhenNoSequence() {
        val thrown = Assert.assertThrows(IllegalStateException::class.java) {
            RouteGraphBuilder()
                .build()
        }
        assertThat(thrown.message).contains("A sequence must be defined for the route.")
    }

    @Test
    fun testSequenceLinearGraph1_noEdgeNoOutput() {
        // There is no "graph edges" to print when there are no edges at all.
        val graph = RouteGraphBuilder()
            .setSequence(listOf(node(1)))
            .build()
        assertThat(graph.toString()).isEqualTo("")
        assertThat(graph.start.block.systemName).isEqualTo("1")
    }

    @Test
    fun testSequenceLinearGraph2() {
        val graph = RouteGraphBuilder()
            .setSequence(listOf(node(1), node(2)))
            .build()
        assertThat(graph.toString()).isEqualTo(
            "[{1}=>{2}]")
        assertThat(graph.start.block.systemName).isEqualTo("1")
        assertThat(graph.toSimulGraph().toString()).isEqualTo(
            "(start=1, nodes=[1, 2], edges=[1=>2])")
    }

    @Test
    fun testSequenceLinearGraph4() {
        val graph = RouteGraphBuilder()
            .setSequence(listOf(node(1), node(2), node(3), node(4)))
            .build()
        assertThat(graph.toString()).isEqualTo(
            "[{1}=>{2}=>{3}=>{4}]")
        assertThat(graph.start.block.systemName).isEqualTo("1")
        assertThat(graph.toSimulGraph().toString()).isEqualTo(
            "(start=1, nodes=[1, 2, 3, 4], edges=[1=>2=>3=>4])")
    }

    @Test
    fun testBranch0_branchTooShort() {
        val n1 = node(1)
        val n2 = node(2)
        val thrown = Assert.assertThrows(IllegalStateException::class.java) {
            RouteGraphBuilder()
                .setSequence(listOf(n1, n2))
                .addBranch(listOf(n1))
                .build()
        }
        assertThat(thrown.message).contains("A branch must have at least 2 nodes (start -> branch -> end)")
    }

    @Test
    fun testBranch0_branchDoesntStartInSequence() {
        val n1 = node(1)
        val n2 = node(2)
        val n3 = node(3)
        val thrown = Assert.assertThrows(IllegalStateException::class.java) {
            RouteGraphBuilder()
                .setSequence(listOf(n1, n2))
                .addBranch(listOf(n3, n2))
                .build()
        }
        assertThat(thrown.message).contains("A branch's first node must be in the sequence graph.")
    }

    @Test
    fun testBranch0_branchDoesntEndInSequence() {
        val n1 = node(1)
        val n2 = node(2)
        val n3 = node(3)
        val thrown = Assert.assertThrows(IllegalStateException::class.java) {
            RouteGraphBuilder()
                .setSequence(listOf(n1, n2))
                .addBranch(listOf(n1, n3))
                .build()
        }
        assertThat(thrown.message).contains("A branch's end node must be in the sequence graph.")
    }

    @Test
    fun testBranch0_branchDupsMainSequence() {
        val n1 = node(1)
        val n2 = node(2)
        val graph = RouteGraphBuilder()
            .setSequence(listOf(n1, n2))
            .addBranch(listOf(n1, n2))
            .build()
        assertThat(graph.toString()).isEqualTo(
            "[{1}=>{2}]")
        assertThat(graph.start.block.systemName).isEqualTo("1")
        assertThat(graph.toSimulGraph().toString()).isEqualTo(
            "(start=1, nodes=[1, 2], edges=[1=>2])")
    }

    @Test
    fun testBranch1() {
        val n1 = node(1)
        val n2 = node(2)
        val n3 = node(3)
        val n4 = node(4)
        val n5 = node(5)
        val n6 = node(6)
        val graph = RouteGraphBuilder()
            .setSequence(listOf(n1, n2, n3, n4))
            .addBranch(listOf(n2, n5, n6, n3))
            .build()
        assertThat(graph.toString()).isEqualTo(
            "[{1}=>{2}=>{3}=>{4}],[{2}->{5}->{6}->{3}]")
        assertThat(graph.start.block.systemName).isEqualTo("1")
        assertThat(graph.toSimulGraph().toString()).isEqualTo(
            "(start=1, nodes=[1, 2, 3, 4, 5, 6], edges=[1=>2=>3=>4],[2->5->6->3])")
    }

    @Test
    fun testBranch3() {
        val n1 = node(1)
        val n2 = node(2)
        val n3 = node(3)
        val n4 = node(4)
        val n5 = node(5)
        val graph = RouteGraphBuilder()
            .setSequence(listOf(n1, n2, n5))
            .addBranch(listOf(n1, n3, n4, n5))
            .addBranch(listOf(n2, n3, n5))
            .addBranch(listOf(n1, n4))
            .build()
        assertThat(graph.toString()).isEqualTo(
            "[{1}=>{2}=>{5}],[{1}->{3}->{4}->{5}],[{1}->{4}],[{2}->{3}->{5}]")
        assertThat(graph.start.block.systemName).isEqualTo("1")
        assertThat(graph.toSimulGraph().toString()).isEqualTo(
            "(start=1, nodes=[1, 2, 5, 3, 4], edges=[1=>2=>5],[1->3->4->5],[1->4],[2->3->5])")
    }

    private fun node(index: Int) =
        NodeBuilder(jmriProvider, blockFactory.get(index.toString())).create()
}
