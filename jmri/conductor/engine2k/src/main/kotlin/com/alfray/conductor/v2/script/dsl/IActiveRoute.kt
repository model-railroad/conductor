package com.alfray.conductor.v2.script.dsl

interface IActiveRoute {
    val active : IRoute
    fun activate(route: IRoute)

    fun idle(): IRoute
    fun sequence(init: IRouteSequenceBuilder.() -> Unit): IRoute
}
