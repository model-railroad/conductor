package com.alfray.conductor.v2.script

import com.alfray.conductor.v2.script.impl.TRuleCondition

interface IConductor {

    fun sensor(systemName: String): ISensor

    fun block(systemName: String): IBlock

    fun turnout(systemName: String): ITurnout

    fun timer(seconds: Int): ITimer

    fun throttle(dccAddress: Int): IThrottle

    fun map(init: SvgMapBuilder.() -> Unit): ISvgMap

    fun on(condition: TRuleCondition): IRule

    val route: RouteBuilder
}
