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

package com.alfray.conductor.v2.script

import com.alflabs.utils.ILogger
import com.alfray.conductor.v2.dagger.Script2kScope
import com.alfray.conductor.v2.script.dsl.Delay
import com.alfray.conductor.v2.script.dsl.ExportedVars
import com.alfray.conductor.v2.script.dsl.IActiveRoute
import com.alfray.conductor.v2.script.dsl.IActiveRouteBuilder
import com.alfray.conductor.v2.script.dsl.IAfter
import com.alfray.conductor.v2.script.dsl.IBlock
import com.alfray.conductor.v2.script.dsl.IConductor
import com.alfray.conductor.v2.script.dsl.IGaEventBuilder
import com.alfray.conductor.v2.script.dsl.IGaPageBuilder
import com.alfray.conductor.v2.script.dsl.IJsonEventBuilder
import com.alfray.conductor.v2.script.dsl.IRule
import com.alfray.conductor.v2.script.dsl.ISensor
import com.alfray.conductor.v2.script.dsl.ISvgMap
import com.alfray.conductor.v2.script.dsl.ISvgMapBuilder
import com.alfray.conductor.v2.script.dsl.IThrottle
import com.alfray.conductor.v2.script.dsl.ITimer
import com.alfray.conductor.v2.script.dsl.ITurnout
import com.alfray.conductor.v2.script.impl.ActiveRouteBuilder
import com.alfray.conductor.v2.script.impl.After
import com.alfray.conductor.v2.script.impl.Factory
import com.alfray.conductor.v2.script.impl.GaEvent
import com.alfray.conductor.v2.script.impl.GaEventBuilder
import com.alfray.conductor.v2.script.impl.GaPage
import com.alfray.conductor.v2.script.impl.GaPageBuilder
import com.alfray.conductor.v2.script.impl.JsonEvent
import com.alfray.conductor.v2.script.impl.JsonEventBuilder
import com.alfray.conductor.v2.script.impl.Rule
import com.alfray.conductor.v2.script.impl.SvgMapBuilder
import com.alfray.conductor.v2.script.impl.Timer
import com.alfray.conductor.v2.utils.assertOrThrow
import javax.inject.Inject

private const val VERBOSE = false

@Script2kScope
class ConductorImpl @Inject internal constructor(
    private val factory: Factory,
    private val logger: ILogger,
    override val exportedVars: ExportedVars,
) : IConductor {
    private val TAG = javaClass.simpleName
    val sensors = mutableMapOf<String, ISensor>()
    val blocks = mutableMapOf<String, IBlock>()
    val turnouts = mutableMapOf<String, ITurnout>()
    val throttles = mutableMapOf<Int, IThrottle>()
    val svgMaps = mutableMapOf<String, ISvgMap>()
    val timers = mutableListOf<ITimer>()
    val rules = mutableListOf<IRule>()
    val activeRoutes = mutableListOf<IActiveRoute>()
    var lastGaPage: GaPage? = null
        private set
    var lastGaEvent: GaEvent? = null
        private set
    var lastJsonEvent: JsonEvent? = null
        private set
    internal val globalContext = ExecContext(ExecContext.State.GLOBAL_SCRIPT)
    private var currentContext = globalContext

    override fun sensor(systemName: String): ISensor {
        if (VERBOSE) logger.d(TAG, "@@ sensor systemName = $systemName")
        return sensors.computeIfAbsent(systemName) { factory.createSensor(it) }
    }

    override fun block(systemName: String): IBlock {
        if (VERBOSE) logger.d(TAG, "@@ block systemName = $systemName")
        return blocks.computeIfAbsent(systemName) { factory.createBlock(it) }
    }

    override fun turnout(systemName: String): ITurnout {
        if (VERBOSE) logger.d(TAG, "@@ turnout systemName = $systemName")
        return turnouts.computeIfAbsent(systemName) { factory.createTurnout(it) }
    }

    override fun timer(delay: Delay): ITimer {
        if (VERBOSE) logger.d(TAG, "@@ timer seconds = $delay")
        val t = Timer(delay)
        timers.add(t)
        return t
    }

    override fun throttle(dccAddress: Int): IThrottle {
        if (VERBOSE) logger.d(TAG, "@@ throttle dccAddress = $dccAddress")
        return throttles.computeIfAbsent(dccAddress) { factory.createThrottle(it) }
    }

    override fun map(svgMapSpecification: ISvgMapBuilder.() -> Unit): ISvgMap {
        if (VERBOSE) logger.d(TAG, "@@ map = $svgMapSpecification")
        val builder = SvgMapBuilder()
        builder.svgMapSpecification()
        val m = builder.create()
        logger.assertOrThrow(TAG, !svgMaps.contains(m.name)) {
            "SvgMap ERROR: Map name ${m.name} is already defined."
        }
        svgMaps[m.name] = m
        return m
    }

    override fun on(condition: () -> Any): IRule {
        if (VERBOSE) logger.d(TAG, "@@ on = $condition")
        logger.assertOrThrow(TAG, currentContext === globalContext) {
            "ERROR: Can only define an on..then rule at the top global level."
        }
        val rule = Rule(condition)
        rules.add(rule)
        return rule
    }

    override fun after(delay: Delay): IAfter {
        return After(delay)
    }

    override fun activeRoute(activeRouteSpecification: IActiveRouteBuilder.() -> Unit): IActiveRoute {
        val b = ActiveRouteBuilder(logger)
        b.activeRouteSpecification()
        val a = b.create()
        activeRoutes.add(a)
        return a
    }

    override fun ga_page(gaPageSpecification: IGaPageBuilder.() -> Unit) {
        if (VERBOSE) logger.d(TAG, "@@ ga_page = $gaPageSpecification")
        val builder = GaPageBuilder()
        builder.gaPageSpecification()
        val pg = builder.create()
        // TODO send page
        lastGaPage = pg
    }

    override fun ga_event(gaEventSpecification: IGaEventBuilder.() -> Unit) {
        if (VERBOSE) logger.d(TAG, "@@ ga_event = $gaEventSpecification")
        val builder = GaEventBuilder()
        builder.gaEventSpecification()
        val ev = builder.create()
        // TODO send event
        lastGaEvent = ev
    }

    override fun json_event(jsonEventSpecification: IJsonEventBuilder.() -> Unit) {
        if (VERBOSE) logger.d(TAG, "@@ json_event = $jsonEventSpecification")
        val builder = JsonEventBuilder()
        builder.jsonEventSpecification()
        val ev = builder.create()
        // TODO send event
        lastJsonEvent = ev
    }

    override fun estop() {
        logger.d(TAG, "@@ TODO estop() is not yet implemented")
    }

    override fun reset_timers(vararg prefix: String) {
        logger.d(TAG, "@@ TODO reset_timers() is not yet implemented (if not obsolete)")
    }

    internal fun changeContext(context: ExecContext) {
        currentContext = context
    }

    internal fun resetContext() {
        currentContext = globalContext
    }
}

