package com.alfray.conductor.v2.script.dsl

import com.alfray.conductor.v2.script.TAction

interface INodeBuilder {
    fun onEnter(action: TAction)
    fun whileOccupied(action: TAction)
    fun onTrailing(action: TAction)
    fun onEmpty(action: TAction)
}

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

interface INode {
}

class Node(builder: NodeBuilder) : INode {
    val block = builder.block
    val actionOnEnter = builder.actionOnEnter
    val actionWhileOccupied = builder.actionWhileOccupied
    val actionOnTrailing = builder.actionOnTrailing
    val actionOnEmpty = builder.actionOnEmpty

    override fun toString(): String {
        return "{${block.systemName}}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node

        if (block != other.block) return false

        return true
    }

    override fun hashCode(): Int {
        return block.hashCode()
    }


}
