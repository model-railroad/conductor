@file:Suppress("FunctionName", "PropertyName")

package com.alfray.conductor.v2.script


data class ExportedVars(
    var Conductor_Time: Int = 0,
    var JSON_URL: String = "",
    var GA_Tracking_Id: String = "",
    var GA_URL: String = "",
    var RTAC_PSA_Text: String = "",
    var RTAC_Motion: Boolean = false,
)

interface IConductor {

    val exportedVars: ExportedVars

    fun sensor(systemName: String): ISensor

    fun block(systemName: String): IBlock

    fun turnout(systemName: String): ITurnout

    fun timer(delay: Delay): ITimer

    fun throttle(dccAddress: Int): IThrottle

    fun map(init: ISvgMapBuilder.() -> Unit): ISvgMap

    fun on(condition: TCondition): IRule

    fun after(delay: Delay): IAfter

    val route: RouteBuilder

    fun activeRoute(init: IActiveRouteBuilder.() -> Unit): IActiveRoute

    fun ga_event(init: IGaEventBuilder.() -> Unit)
    fun json_event(init: IJsonEventBuilder.() -> Unit)
}
