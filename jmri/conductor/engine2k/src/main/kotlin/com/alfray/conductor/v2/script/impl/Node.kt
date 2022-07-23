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
import com.alfray.conductor.v2.script.dsl.INode
import com.alfray.conductor.v2.script.dsl.TAction

/** Internal DSL script implementation for a route sequence node. */
internal class Node(builder: NodeBuilder) : INode {
    override val block = builder.block
    val actionOnEnter = builder.actionOnEnter
    val actionWhileOccupied = builder.actionWhileOccupied
    val actionOnTrailing = builder.actionOnTrailing
    val actionOnEmpty = builder.actionOnEmpty

    private var callOnEnter: TAction? = null
    private var callWhileOccupied: TAction? = null
    private var callOnTrailing: TAction? = null
    private var callOnEmpty: TAction? = null
    private val context = ExecContext(ExecContext.State.NODE)

    override fun toString(): String {
        return "{${block.systemName}}"
    }

    fun changeState(newState: Block.State) {
        block as Block
        val oldState = block.state
        if (oldState == newState) {
            if (newState == Block.State.OCCUPIED) {
                callWhileOccupied = actionWhileOccupied
//                context.changeState(ExecContext.State.NODE_OCCUPIED)
            }
        } else {
            if (newState == Block.State.OCCUPIED) {
                callOnEnter = actionOnEnter
                callWhileOccupied = actionWhileOccupied
//                context.changeState(ExecContext.State.NODE_ENTER) // TODO is that state useful?
            } else if (newState == Block.State.TRAILING) {
                callOnTrailing = actionOnTrailing
//                context.changeState(ExecContext.State.NODE_TRAILING)
            } else if (newState == Block.State.EMPTY) {
                callOnEmpty = actionOnEmpty
//                context.changeState(ExecContext.State.NODE_EMPTY)
            }
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
