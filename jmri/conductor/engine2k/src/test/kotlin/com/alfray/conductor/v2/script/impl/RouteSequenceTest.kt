package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.dsl.INode
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
            "[{1}]")
    }

    @Test
    fun testSequenceToLinearGraph2() {
        val start = RouteSequence.sequenceToLinearGraph(
            listOf(node(1), node(2)))
        assertThat(RouteSequence.printGraph(start)).isEqualTo(
            "[{1}>>{2}] -> [{1}>{2}]")
    }

    @Test
    fun testSequenceToLinearGraph4() {
        val start = RouteSequence.sequenceToLinearGraph(
            listOf(node(1), node(2), node(3), node(4)))
        assertThat(RouteSequence.printGraph(start)).isEqualTo(
            "[{1}>>{2}] -> [{1}>{2}>>{3}] -> [{2}>{3}>>{4}] -> [{3}>{4}]")
    }

    @Test
    fun testBranch1() {
        val start = RouteSequence.sequenceToLinearGraph(
            listOf(node(1), node(2), node(3), node(4)))
        RouteSequence.addGraphBranch(start,
            listOf(node(2), node(5), node(6), node(3)))
        assertThat(RouteSequence.printGraph(start)).isEqualTo(
            "[{1}>>{2}] -> [{1}>{2}>>{3}+{5}] -> [{2}>{3}>>{4}] -> [{3}>{4}] "
            + "-> [{2}>{5}>>{6}] -> [{5}>{6}>>{3}]")
    }

    private fun node(index: Int) = TNode(index)

    class TNode(val index: Int) : INode {
        override fun toString(): String {
            return "{$index}"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TNode

            if (index != other.index) return false

            return true
        }

        override fun hashCode(): Int {
            return index
        }

    }
}
