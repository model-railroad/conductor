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

import com.alfray.conductor.v2.script.dsl.INode
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Data class collecting stats for a route and exporting the data as JSON.
 *
 * All JSON exported fields must be public or annotated with @JsonProperty.
 */
class SequenceRouteStats(
    val name: String,                           // field name exported to JSON
) {
    companion object {
        data class NodeTiming(
            var n: String,                      // field name exported to JSON
            val ms: Long)                       // field name exported to JSON
    }

    @JsonProperty("act")
    private var numActivations = 0
    @JsonProperty("err")
    private var isError = false
    @JsonProperty("nodes")
    private val nodes = mutableListOf<NodeTiming>()


    fun activateAndReset() {
        numActivations++
        isError = false
        nodes.clear()
    }

    fun setError() {
        isError = true
    }

    fun addNode(node: INode) {
        val block = node.block as BlockBase
        addNodeWithDurationMs(node, block.stateTimeMs())
    }

    fun addNodeWithDurationMs(node: INode, durationMs: Long) {
        nodes.add(NodeTiming(node.block.name, durationMs))
    }

    fun toJsonString(): String {
        // Adjust the block names to avoid duplicates.
        // Count the number of time each node name is present. Must be 1 or more.
        val blockCount = mutableMapOf<String, Int>()
        nodes.forEach {
            blockCount.merge(it.n, 1) { a, b -> a + b }
        }
        // For each block name present more than once, generate a new name "name.1", "name.2" etc.
        val blockNext = mutableMapOf<String, Int>()
        nodes.forEach {
            val name = it.n
            if (blockCount[name]!! > 1) {
                blockNext.merge(name, 1) { a, b -> a + b }
                val index = blockNext[name]!!
                it.n = "$name.$index"
            }
        }

        val mapper = ObjectMapper()
        return mapper.writeValueAsString(this)
    }
}
