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
import com.alfray.conductor.v2.script.dsl.IActiveRoute
import com.alfray.conductor.v2.script.dsl.IBlock
import com.alfray.conductor.v2.script.dsl.INode
import com.alfray.conductor.v2.script.dsl.INodeBuilder
import com.alfray.conductor.v2.script.dsl.IRouteSequence
import com.alfray.conductor.v2.script.dsl.IRouteSequenceBuilder
import com.alfray.conductor.v2.script.dsl.IThrottle

internal class RouteSequenceBuilder(
    logger: ILogger,
    owner: IActiveRoute,
) : RouteBaseBuilder(logger, owner), IRouteSequenceBuilder {
    private val TAG = javaClass.simpleName
    override val route: IActiveRoute
        get() = owner
    override lateinit var throttle: IThrottle
    override var timeout = 60
    override lateinit var sequence: List<INode>
    override val branches = mutableListOf<List<INode>>()

    override fun node(block: IBlock, init: INodeBuilder.() -> Unit): INode {
        val b = NodeBuilder(logger, block)
        b.init()
        return Node(b)
    }

    fun create() : IRouteSequence = RouteSequence(owner, this)
}
