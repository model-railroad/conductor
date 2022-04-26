package com.alfray.conductor.v2.script.dsl

import com.alfray.conductor.v2.script.TAction
import com.alfray.conductor.v2.script.impl.RouteSequence

interface IRouteSequenceBuilder {
    val route: IActiveRoute
    var throttle: IThrottle
    var timeout: Int
    var nodes: List<Any>
    fun onActivate(action: TAction)
    fun node(block: IBlock, init: INodeBuilder.() -> Unit) : INode
}

class RouteSequenceBuilder(private val owner: IActiveRoute) : IRouteSequenceBuilder {
    override val route: IActiveRoute
        get() = owner
    override lateinit var throttle: IThrottle
    override var timeout = 60
    override var nodes: List<Any> = emptyList()
    var actionOnActivate = RuleActionEmpty

    override fun onActivate(action: TAction) {
        check(actionOnActivate == RuleActionEmpty)
        actionOnActivate = action
    }

    override fun node(block: IBlock, init: INodeBuilder.() -> Unit): INode {
        val b = NodeBuilder(block)
        b.init()
        return Node(b)
    }

    fun create() : IRoute = RouteSequence(this)
}
