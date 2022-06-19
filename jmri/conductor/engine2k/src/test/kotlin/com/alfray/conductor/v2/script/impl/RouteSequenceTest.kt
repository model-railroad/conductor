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
import com.alfray.conductor.v2.script.dsl.NodeBuilder
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import dagger.internal.InstanceFactory
import org.junit.Before
import org.junit.Test

class RouteSequenceTest {
    private lateinit var blockFactory: Block_Factory
    private val jmriProvider = FakeJmriProvider()
    private val eventLogger = mock<EventLogger>()
    private val keyValue = mock<IKeyValue>()

    @Before
    fun setUp() {
        blockFactory = Block_Factory(
            InstanceFactory.create(keyValue),
            InstanceFactory.create(eventLogger),
            InstanceFactory.create(jmriProvider))
    }

    @Test
    fun testSequenceToLinearGraph1() {
        val start = RouteSequence.sequenceToLinearGraph(
            listOf(node(1)))
        assertThat(RouteSequence.printGraph(start)).isEqualTo(
            "[{1}<>]")
    }

    @Test
    fun testSequenceToLinearGraph2() {
        val start = RouteSequence.sequenceToLinearGraph(
            listOf(node(1), node(2)))
        assertThat(RouteSequence.printGraph(start)).isEqualTo(
            "[{1}->{2}] -> [{2}<>]")
    }

    @Test
    fun testSequenceToLinearGraph4() {
        val start = RouteSequence.sequenceToLinearGraph(
            listOf(node(1), node(2), node(3), node(4)))
        assertThat(RouteSequence.printGraph(start)).isEqualTo(
            "[{1}->{2}] -> [{2}->{3}] -> [{3}->{4}] -> [{4}<>]")
    }

    @Test
    fun testBranch1() {
        val n1 = node(1)
        val n2 = node(2)
        val n3 = node(3)
        val n4 = node(4)
        val start = RouteSequence.sequenceToLinearGraph(
            listOf(n1, n2, n3, n4))
        val n5 = node(5)
        val n6 = node(6)
        RouteSequence.addGraphBranch(start,
            listOf(n2, n5, n6, n3))
        assertThat(RouteSequence.visitGraph(start).map { it.toString() })
            .containsExactly(
                "[{1}->{2}]",
                "[{2}->{3}+{5}]",
                "[{3}->{4}]",
                "[{4}<>]",
                "[{5}->{6}]",
                "[{6}->{3}]")
            .inOrder()
    }

    @Test
    fun testBranch3() {
        val n1 = node(1)
        val n2 = node(2)
        val n3 = node(3)
        val n4 = node(4)
        val n5 = node(5)
        val start = RouteSequence.sequenceToLinearGraph(
            listOf(n1, n2, n5))
        RouteSequence.addGraphBranch(start, listOf(n1, n3, n4, n5))
        RouteSequence.addGraphBranch(start, listOf(n2, n3, n5))
        RouteSequence.addGraphBranch(start, listOf(n1, n4))
        assertThat(RouteSequence.visitGraph(start).map { it.toString() })
            .containsExactly(
                "[{1}->{2}+{3}+{4}]",
                "[{2}->{5}+{3}]",
                "[{5}<>]",
                "[{3}->{4}]",
                "[{4}->{5}]",
                "[{3}->{5}]")
            .inOrder()
    }

    private fun node(index: Int) =
        NodeBuilder(blockFactory.get(index.toString())).create()
}
