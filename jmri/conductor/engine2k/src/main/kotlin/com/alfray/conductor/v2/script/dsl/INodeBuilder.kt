package com.alfray.conductor.v2.script.dsl

/** DSL script interface to build a route [INode]. */
interface INodeBuilder {
    /**
     * The minimum time once a block has been entered before we can reach the next block.
     *
     * It must cover at least the time needed to initially fully enter the block.
     * Any "flaky" sensors in both the occupied and trailing blocks are ignored during that time.
     * The default minimum time for the route's blocks is 10 seconds, unless changed in the
     * route or in its nodes.
     * When this is set to 0 (the default), the route's minSecondsOnBlock is used instead.
     */
    var minSecondsOnBlock: Int

    /**
     * The maximum time spent moving on the currently occupied block.
     *
     * Any "flaky" sensor in the occupied block are ignored during that time.
     * The default timeout for the route's blocks is 60 seconds, unless changed in the
     * route or in its nodes.
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
