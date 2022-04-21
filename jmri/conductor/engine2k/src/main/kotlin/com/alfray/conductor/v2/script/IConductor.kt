package com.alfray.conductor.v2.script

interface IConductor {
    fun sensor(systemName: String): ISensor

    fun block(systemName: String): IBlock

    fun turnout(systemName: String): ITurnout

    fun timer(seconds: Int): ITimer

    fun throttle(dccAddress: Int): IThrottle

    fun map(init: ISvgMapBuilder.() -> Unit): ISvgMap

    fun on(condition: TCondition): IRule

    fun after(timer: ITimer): IAfter

    val route: RouteBuilder

    fun activeRoute(init: IActiveRouteBuilder.() -> Unit): IActiveRoute
}
