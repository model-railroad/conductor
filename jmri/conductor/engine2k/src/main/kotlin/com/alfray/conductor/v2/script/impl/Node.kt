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

import com.alfray.conductor.v2.script.ExecContext
import com.alfray.conductor.v2.script.dsl.INode

/** Internal DSL script implementation for a route sequence node. */
internal class Node(builder: NodeBuilder) : INode {
    override val block = builder.block
    val actionOnEnter = builder.actionOnEnter
    val actionWhileOccupied = builder.actionWhileOccupied
    val actionOnTrailing = builder.actionOnTrailing
    val actionOnEmpty = builder.actionOnEmpty
    internal val context = object: ExecContext(ExecContext.State.UNKNOWN) {
        override fun onStateChanged(oldState: State, newState: State) {
            TODO("Not yet implemented")
        }
    }

    override fun toString(): String {
        return "{${block.systemName}}"
    }
}
