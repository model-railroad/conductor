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

import com.alflabs.utils.ILogger
import com.alfray.conductor.v2.script.dsl.IBlock
import com.alfray.conductor.v2.script.dsl.INode
import com.alfray.conductor.v2.script.dsl.INodeBuilder
import com.alfray.conductor.v2.script.dsl.TAction
import com.alfray.conductor.v2.utils.assertOrThrow
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Internal DSL script builder for [INode]. */
class NodeBuilder @AssistedInject constructor(
    private val logger: ILogger,
    @Assisted val block: IBlock
) : INodeBuilder {
    private val TAG = javaClass.simpleName
    var actionOnEnter: TAction? = null
    var actionWhileOccupied: TAction? = null
    var actionOnTrailing: TAction? = null
    var actionOnEmpty: TAction? = null
    override var minSecondsOnBlock = 0
    override var maxSecondsOnBlock = 0

    override fun onEnter(action: TAction) {
        logger.assertOrThrow(TAG, actionOnEnter == null) {
            "Node onEnter defined more than once"
        }
        actionOnEnter = action
    }

    override fun whileOccupied(action: TAction) {
        logger.assertOrThrow(TAG, actionWhileOccupied == null) {
            "Node whileOccupied defined more than once"
        }
        actionWhileOccupied = action
    }

    override fun onTrailing(action: TAction) {
        logger.assertOrThrow(TAG, actionOnTrailing == null) {
            "Node onTrailing defined more than once"
        }
        actionOnTrailing = action
    }

    override fun onEmpty(action: TAction) {
        logger.assertOrThrow(TAG, actionOnEmpty == null) {
            "Node onEmpty defined more than once"
        }
        actionOnEmpty = action
    }

    fun create() : INode = Node(this)
}
