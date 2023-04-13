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

import com.alfray.conductor.v2.script.ExecAction
import com.alfray.conductor.v2.script.ExecContext
import com.alfray.conductor.v2.script.dsl.IBlock
import com.alfray.conductor.v2.script.dsl.INode
import com.alfray.conductor.v2.script.dsl.TAction

/**
 * Internal DSL script implementation for a sequence route node.
 *
 * Warning on node's equality: in some context "node equality" means strict object
 * reference equality, yet in some other contexts it is best understood as "underlying
 * block equality".
 * Suggestion:
 * - When comparing nodes, always use strict object equality (i.e. node1 === node2).
 * - When *block equality* is desired, make that explicit by comparing node.block elements.
 * - Make that explicit via clear variable names (e.g. listOfBlocks vs listOfNodes).
 */
internal class Node(builder: NodeBuilder) : INode {
    override val block = builder.block
    val actionOnEnter = builder.actionOnEnter
    val actionWhileOccupied = builder.actionWhileOccupied
    val actionOnTrailing = builder.actionOnTrailing
    val actionOnEmpty = builder.actionOnEmpty

    /**
     * A shuttle node is a "reversal" node where a shuttle stops and reverses direction.
     * In a "reversal node", the incoming node and the outgoing nodes point to the same
     * underlying block, even though the nodes themselves may be different.
     * This attribute is computed by the [SequenceRouteBuilder] and can only figure out once
     * we have the entire route mapped.
     */
    var reversal: Boolean? = null

    private var callOnEnter: TAction? = null
    private var callWhileOccupied: TAction? = null
    private var callOnTrailing: TAction? = null
    private var callOnEmpty: TAction? = null
    val context = ExecContext(ExecContext.Reason.NODE, this)

    override fun toString(): String {
        return if (reversal != null && reversal == true) {
            "<${block.name}>"
        } else {
            "{${block.name}}"
        }
    }

    /** Change the Node's block's state, and triggers the onEvent callbacks as appropriate
     * for the next execution. */
    fun changeState(newState: IBlock.State) {
        val oldState = block.state
        if (oldState == newState) {
            if (newState == IBlock.State.OCCUPIED) {
                callWhileOccupied = actionWhileOccupied
            }
        } else {
            // Clear all context timers when changing state.
            // We do not clear timers when going from "Enter" to "Occupied" since they are
            // actually both represented by the same state "Occupied".
            context.clearTimers()

            when (newState) {
                IBlock.State.OCCUPIED -> {
                    callOnEnter = actionOnEnter
                    callWhileOccupied = actionWhileOccupied
                }
                IBlock.State.TRAILING -> {
                    callOnTrailing = actionOnTrailing
                }
                IBlock.State.EMPTY -> {
                    callOnEmpty = actionOnEmpty
                }
            }

            block as INodeBlock
            block.changeState(newState)
        }
    }

    /** If a Node's block's state is in OCCUPIED state, triggers its onEnter for next execution. */
    fun changeEnterState() {
        block as INodeBlock
        if (block.state == IBlock.State.OCCUPIED) {
            callOnEnter = actionOnEnter
            callWhileOccupied = actionWhileOccupied
        }
    }

    fun collectActions(execActions: MutableList<ExecAction>) {
        callOnEnter?.let {
            execActions.add(ExecAction(context, it))
            callOnEnter = null
        }
        callWhileOccupied?.let {
            execActions.add(ExecAction(context, it))
            callWhileOccupied = null
        }
        callOnTrailing?.let {
            execActions.add(ExecAction(context, it))
            callOnTrailing = null
        }
        callOnEmpty?.let {
            execActions.add(ExecAction(context, it))
            callOnEmpty = null
        }
    }
}
