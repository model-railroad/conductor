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

import com.alflabs.utils.IClock
import com.alfray.conductor.v2.script.dsl.INode
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Data class collecting stats for a route and exporting the data as JSON.
 *
 * All JSON exported fields must be public or annotated with @JsonProperty.
 *
 * @param routeName The name of the route exported to JSON.
 * @param throttleName The name of the throttle exported to JSON.
 */
class SequenceRouteStats(
    private val clock: IClock,
    val routeName: String,
    val throttleName: String,
) {
    /**
     * One node and its occupation duration in milliseconds.
     *
     * @param n The name of the node, exported to JSON.
     * @param ms The occupation duration in milliseconds, exported to JSON.
     */
    data class NodeTiming(
        var n: String,                      // field name exported to JSON
        val ms: Long)                       // field name exported to JSON

    enum class Running {
        Unknown,
        Started,
        Ended,
    }

    private var numActivations = 0
    private var isError = false
    private var running = Running.Unknown
    private var startTS = 0L
    private var endTS = 0L
    private val nodes = mutableListOf<NodeTiming>()

    data class JsonStructureV1(
        // All field names directly as exported to JSON
        val name: String,
        val th: String,
        val act: Int,
        val err: Boolean,
        val nodes: List<NodeTiming>,
    )

    fun activateAndReset() {
        numActivations++
        isError = false
        nodes.clear()
        setRunning(Running.Started)
    }

    fun setRunning(running: Running) {
        this.running = running
        when (running) {
            Running.Started -> startTS = clock.elapsedRealtime()
            Running.Ended ->   endTS   = clock.elapsedRealtime()
            Running.Unknown -> { /*no-op*/ }
        }
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
        return mapper.writeValueAsString(JsonStructureV1(
            routeName,
            throttleName,
            numActivations,
            isError,
            nodes
        ))
    }
}
