package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.dsl.NodeBuilder
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RouteSequenceTest {
    @Before
    fun setUp() {
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
        NodeBuilder(Block(index.toString())).create()
}
