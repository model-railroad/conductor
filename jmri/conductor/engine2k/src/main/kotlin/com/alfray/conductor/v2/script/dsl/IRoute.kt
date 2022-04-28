@file:Suppress("FunctionName")

package com.alfray.conductor.v2.script.dsl

interface IRoute {
    /** The [IActiveRoute] containing this route. */
    val owner: IActiveRoute

    /** Activates this route in its [IActiveRoute]. */
    fun activate()

    /** Changes the default starting block for this route.
     * Only effective if called from the onActivate callback. */
    fun start_node(node: INode)
}
