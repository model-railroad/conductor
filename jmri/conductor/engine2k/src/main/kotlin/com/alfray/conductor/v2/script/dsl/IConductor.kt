/*
 * Project: Conductor
 * Copyright (C) 2022 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

@file:Suppress("FunctionName", "PropertyName")

package com.alfray.conductor.v2.script.dsl

/** Base interface for the Conductor script. */
interface IConductor {

    /** Variables exchanged with the Conductor engine and exported via the KV Server. */
    val exportedVars: ExportedVars

    /** Logs a string to the console and the recorded log. */
    fun log(message: String)

    /** Registers a new sensor with the given system name.
     * Once registered, the same sensor object is reused for the same system name. */
    fun sensor(systemName: String): ISensor

    /** Registers a new block with the given system name.
     * Once registered, the same block object is reused for the same system name. */
    fun block(systemName: String): IBlock

    /** Registers a new virtual block with the given system name.
     * Once registered, the same virtual block object is reused for the same system name. */
    fun virtualBlock(systemName: String): IBlock

    /** Registers a new turnout with the given system name.
     * Once registered, the same turnout object is reused for the same system name. */
    fun turnout(systemName: String): ITurnout

    /** Registers a new throttle with the given DCC Address.
     * Once registered, the same throttle object is reused for the same system name. */
    fun throttle(dccAddress: Int): IThrottle

    /** Creates a new timer object with the given delay. */
    fun timer(delay: Delay): ITimer

    /** Registers a new map. */
    fun map(svgMapSpecification: ISvgMapBuilder.() -> Unit): ISvgMap

    /** Creates a new on..then rule with the specified conditions and actions.
     * Can only be used at the top level / global scope. */
    fun on(condition: TCondition): IOnRule

    /** Creates a new on..then rule with the specified conditions and actions.
     * The condition is only evaluated after the delay has expired.
     * The timer starts immediately when the engine starts (after parsing is finished).
     * Can only be used at the top level / global scope. */
    fun on(delay: Delay, condition: TCondition): IOnRule

    /** Creates a new delayed rule active after the specified delay. */
    fun after(delay: Delay): IAfter

    /** Sends an emergency E-STOP to the DCC controller. */
    fun eStop()

    /** Creates a new RoutesContainer to select between multiple routes. */
    fun routes(routesContainerSpecification: IRoutesContainerBuilder.() -> Unit): IRoutesContainer

    /** Sends a GA Page statistic.
     * No-op till GA ID & URL are defined. */
    fun gaPage(gaPageSpecification: IGaPageBuilder.() -> Unit)

    /** Sends a GA Event statistic.
     * No-op till GA ID & URL are defined. */
    fun gaEvent(gaEventSpecification: IGaEventBuilder.() -> Unit)

    /** Sends a JSON status.
     * No-op till the JSON URL is defined. */
    fun jsonEvent(jsonEventSpecification: IJsonEventBuilder.() -> Unit)
}
