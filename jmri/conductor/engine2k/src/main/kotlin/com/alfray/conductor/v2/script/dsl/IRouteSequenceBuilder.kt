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

package com.alfray.conductor.v2.script.dsl

import com.alfray.conductor.v2.script.TAction
import com.alfray.conductor.v2.script.impl.RouteSequence

interface IRouteSequenceBuilder {
    val route: IActiveRoute
    var throttle: IThrottle
    var timeout: Int
    var sequence: List<INode>
    val branches: MutableList<List<INode>>
    fun onActivate(action: TAction)
    fun onRecover(action: TAction)
    fun node(block: IBlock, init: INodeBuilder.() -> Unit) : INode
}

internal class RouteSequenceBuilder(private val owner: IActiveRoute) : IRouteSequenceBuilder {
    override val route: IActiveRoute
        get() = owner
    override lateinit var throttle: IThrottle
    override var timeout = 60
    override lateinit var sequence: List<INode>
    override val branches = mutableListOf<List<INode>>()
    var actionOnActivate = RuleActionEmpty
    var actionOnRecover = RuleActionEmpty

    override fun onActivate(action: TAction) {
        check(actionOnActivate == RuleActionEmpty)
        actionOnActivate = action
    }

    override fun onRecover(action: TAction) {
        check(actionOnRecover == RuleActionEmpty)
        actionOnRecover = action
    }

    override fun node(block: IBlock, init: INodeBuilder.() -> Unit): INode {
        val b = NodeBuilder(block)
        b.init()
        return Node(b)
    }

    fun create() : IRouteSequence = RouteSequence(owner, this)
}
