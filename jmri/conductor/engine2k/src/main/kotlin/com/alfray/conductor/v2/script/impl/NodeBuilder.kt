package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.TAction
import com.alfray.conductor.v2.script.dsl.IBlock
import com.alfray.conductor.v2.script.dsl.INode
import com.alfray.conductor.v2.script.dsl.Node
import com.alfray.conductor.v2.script.dsl.RuleActionEmpty

/** DSL script interface to build a route [INode]. */
interface INodeBuilder {
    fun onEnter(action: TAction)
    fun whileOccupied(action: TAction)
    fun onTrailing(action: TAction)
    fun onEmpty(action: TAction)
}

/** Internal DSL script builder for [INode]. */
class NodeBuilder(val block: IBlock) : INodeBuilder {
    var actionOnEnter = RuleActionEmpty
    var actionWhileOccupied = RuleActionEmpty
    var actionOnTrailing = RuleActionEmpty
    var actionOnEmpty = RuleActionEmpty

    override fun onEnter(action: TAction) {
        check(actionOnEnter == RuleActionEmpty)
        actionOnEnter = action
    }

    override fun whileOccupied(action: TAction) {
        check(actionWhileOccupied == RuleActionEmpty)
        actionWhileOccupied = action
    }

    override fun onTrailing(action: TAction) {
        check(actionOnTrailing == RuleActionEmpty)
        actionOnTrailing = action
    }

    override fun onEmpty(action: TAction) {
        check(actionOnEmpty == RuleActionEmpty)
        actionOnEmpty = action
    }

    fun create() : INode = Node(this)
}
