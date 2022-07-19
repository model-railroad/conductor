package com.alfray.conductor.v2.script.dsl

/** DSL script interface to build a route [INode]. */
interface INodeBuilder {
    fun onEnter(action: TAction)
    fun whileOccupied(action: TAction)
    fun onTrailing(action: TAction)
    fun onEmpty(action: TAction)
}
