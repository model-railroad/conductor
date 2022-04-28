@file:Suppress("FunctionName", "PropertyName")

package com.alfray.conductor.v2.script.dsl

import com.alfray.conductor.v2.script.TCondition

/** Variables exchanged with the Conductor engine and exported via the KV Server. */
data class ExportedVars(
    /** Current time in HHMM format set by the conductor engine. Read-only. */
    var Conductor_Time: Int = 0,
    /** URL to the JSON server. Written by the script.
     * The JSON server is inactive till this defined. */
    var JSON_URL: String = "",
    /** ID for the GA server. Written by the script.
     * GA Events are not sent until this is defined. */
    var GA_Tracking_Id: String = "",
    /** Site URL for the GA server. Written by the script.
     * GA Events are not sent until this is defined. */
    var GA_URL: String = "",
    /** Announcement text sent to the remote RTAC tablet android software.
     * Written by the script. Sent via the KV Server. */
    var RTAC_PSA_Text: String = "",
    /** Motion indication sent to the remote RTAC tablet android software.
     * Written by the script. Sent via the KV Server. */
    var RTAC_Motion: Boolean = false,
)

/** Base interface for the Conductor script. */
interface IConductor {

    /** Variables exchanged with the Conductor engine and exported via the KV Server. */
    val exportedVars: ExportedVars

    /** Registers a new sensor with the given system name.
     * Once registered, the same sensor object is reused for the same system name. */
    fun sensor(systemName: String): ISensor

    /** Registers a new block with the given system name.
     * Once registered, the same block object is reused for the same system name. */
    fun block(systemName: String): IBlock

    /** Registers a new turnout with the given system name.
     * Once registered, the same turnout object is reused for the same system name. */
    fun turnout(systemName: String): ITurnout

    /** Registers a new throttle with the given DCC Address.
     * Once registered, the same throttle object is reused for the same system name. */
    fun throttle(dccAddress: Int): IThrottle

    /** Creates a new timer object with the given delay. */
    fun timer(delay: Delay): ITimer

    /** Reset all timers starting with the given prefixes.
     * OBSOLETE from Conductor1, clean-up. */
    fun reset_timers(vararg prefix: String)

    /** Registers a new map. */
    fun map(init: ISvgMapBuilder.() -> Unit): ISvgMap

    /** Creates a new rule with the specified conditions and actions. */
    fun on(condition: TCondition): IRule

    /** Creates a new delayed rule active after the specified delay. */
    fun after(delay: Delay): IAfter

    /** Sends an emergency E-STOP to the DCC controller. */
    fun estop()

    /** Creates a new ActiveRoute to select between multiple routes. */
    fun activeRoute(init: IActiveRouteBuilder.() -> Unit): IActiveRoute

    /** Sends a GA Page statistic.
     * No-op till GA ID & URL are defined. */
    fun ga_page(init: IGaPageBuilder.() -> Unit)

    /** Sends a GA Event statistic.
     * No-op till GA ID & URL are defined. */
    fun ga_event(init: IGaEventBuilder.() -> Unit)

    /** Sends a JSON status.
     * No-op till the JSON URL is defined. */
    fun json_event(init: IJsonEventBuilder.() -> Unit)
}
