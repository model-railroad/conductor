package com.alfray.conductor.v2.script.dsl

interface IActiveRoute {
    /** The currently active route. */
    val active : IRoute
    /** Activates this route. */
    fun activate(route: IRoute)

    /** The current route and this are in error. */
    val error : Boolean

    /** Registers a new idle route. */
    fun idle(): IRoute
    /** Registers a new sequence route. */
    fun sequence(init: IRouteSequenceBuilder.() -> Unit): IRoute
}
