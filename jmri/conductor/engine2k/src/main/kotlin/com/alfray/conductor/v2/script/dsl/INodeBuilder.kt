package com.alfray.conductor.v2.script.dsl

/** DSL script interface to build a route [INode]. */
interface INodeBuilder {
    /**
     * The maximum time spent moving on this currently occupied block.
     * Timeout is reset when the train stops.
     * When this is set to 0 (the default), the route's maxSecondsOnBlock is used instead.
     */
    var maxSecondsOnBlock: Int

    /** Callback executed once when a new block becomes occupied. */
    fun onEnter(action: TAction)

    /**
     * Callback executed repeatedly every cycle as long as the block is occupied.
     *
     * Scripts should be carefuly to not allocate objects repeatedly as this is called in a loop
     * at 30 Hz. Some DSL methods such as "on rules" and "after rules" are forbidden in this
     * context as they invariably create new underlying objects when called.
     */
    fun whileOccupied(action: TAction)

    /** Callback executed once when the currently occupied block becomes trailing. */
    fun onTrailing(action: TAction)

    /** Callback executed once when the currently trailing block becomes empty. */
    fun onEmpty(action: TAction)
}
