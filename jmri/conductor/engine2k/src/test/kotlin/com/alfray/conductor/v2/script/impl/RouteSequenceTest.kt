package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.dsl.INode
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Ignore
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
        val seq = listOf(node(1))
        val start = RouteSequence.sequenceToLinearGraph(seq)
        assertThat(RouteSequence.printGraph(start)).isEqualTo("[{1}]")
    }

    @Test
    fun testSequenceToLinearGraph2() {
        val seq = listOf(node(1), node(2))
        val start = RouteSequence.sequenceToLinearGraph(seq)
        assertThat(RouteSequence.printGraph(start)).isEqualTo(
            "[{1}>>{2}] -> [{1}>{2}]")
    }

    @Test
    fun testSequenceToLinearGraph4() {
        val seq = listOf(node(1), node(2), node(3), node(4))
        val start = RouteSequence.sequenceToLinearGraph(seq)
        assertThat(RouteSequence.printGraph(start)).isEqualTo(
            "[{1}>>{2}] -> [{1}>{2}>>{3}] -> [{2}>{3}>>{4}] -> [{3}>{4}]")
    }

    private fun node(index: Int) = TNode(index)

    class TNode(val index: Int) : INode {
        override fun toString(): String {
            return "{$index}"
        }
    }
}
