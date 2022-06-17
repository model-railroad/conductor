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
import com.alfray.conductor.v2.script.impl.ActiveRoute
import com.alfray.conductor.v2.script.impl.ActiveRouteBuilder
import com.alfray.conductor.v2.script.impl.After
import com.alfray.conductor.v2.script.impl.Block
import com.alfray.conductor.v2.script.impl.GaEvent
import com.alfray.conductor.v2.script.impl.GaEventBuilder
import com.alfray.conductor.v2.script.impl.GaPage
import com.alfray.conductor.v2.script.impl.GaPageBuilder
import com.alfray.conductor.v2.script.impl.IBlockFactory
import com.alfray.conductor.v2.script.impl.ISensorFactory
import com.alfray.conductor.v2.script.impl.IThrottleFactory
import com.alfray.conductor.v2.script.impl.ITurnoutFactory
import com.alfray.conductor.v2.script.impl.JsonEvent
import com.alfray.conductor.v2.script.impl.JsonEventBuilder
import com.alfray.conductor.v2.script.impl.Rule
import com.alfray.conductor.v2.script.impl.SvgMapBuilder
import com.alfray.conductor.v2.script.impl.Timer
import javax.inject.Inject

private const val VERBOSE = false

@Script2kScope
class ConductorImpl @Inject internal constructor(
    private val blockFactory: IBlockFactory,
    private val sensorFactory: ISensorFactory,
    private val turnoutFactory: ITurnoutFactory,
    private val throttleFactory: IThrottleFactory,
) : IConductor {

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

    override val exportedVars = ExportedVars()

    override fun sensor(systemName: String): ISensor {
        if (VERBOSE) println("@@ sensor systemName = $systemName")
        return sensors.computeIfAbsent(systemName) { sensorFactory.create(it) }
    }

    override fun block(systemName: String): IBlock {
        if (VERBOSE) println("@@ block systemName = $systemName")
        return blocks.computeIfAbsent(systemName) { blockFactory.create(it) }
    }

    override fun turnout(systemName: String): ITurnout {
        if (VERBOSE) println("@@ turnout systemName = $systemName")
        return turnouts.computeIfAbsent(systemName) { turnoutFactory.create(it) }
    }

    override fun timer(delay: Delay): ITimer {
        if (VERBOSE) println("@@ timer seconds = $delay")
        val t = Timer(delay)
        timers.add(t)
        return t
    }

    override fun throttle(dccAddress: Int): IThrottle {
        if (VERBOSE) println("@@ throttle dccAddress = $dccAddress")
        return throttles.computeIfAbsent(dccAddress) { throttleFactory.create(it) }
    }

    override fun map(init: ISvgMapBuilder.() -> Unit): ISvgMap {
        if (VERBOSE) println("@@ map = $init")
        val builder = SvgMapBuilder()
        builder.init()
        val m = builder.create()
        if (svgMaps.contains(m.name)) {
            throw IllegalArgumentException ("Map name ${m.name} is already defined.")
        }
        svgMaps[m.name] = m
        return m
    }

    override fun on(condition: () -> Any): IRule {
        if (VERBOSE) println("@@ on = $condition")
        val rule = Rule(condition)
        rules.add(rule)
        return rule
    }

    override fun after(delay: Delay): IAfter {
        return After(delay)
    }

    override fun activeRoute(init: IActiveRouteBuilder.() -> Unit): IActiveRoute {
        val b = ActiveRouteBuilder()
        b.init()
        val a = ActiveRoute(b)
        activeRoutes.add(a)
        return a
    }

    override fun ga_page(init: IGaPageBuilder.() -> Unit) {
        if (VERBOSE) println("@@ ga_page = $init")
        val builder = GaPageBuilder()
        builder.init()
        val pg = builder.create()
        // TODO send page
        lastGaPage = pg
    }

    override fun ga_event(init: IGaEventBuilder.() -> Unit) {
        if (VERBOSE) println("@@ ga_event = $init")
        val builder = GaEventBuilder()
        builder.init()
        val ev = builder.create()
        // TODO send event
        lastGaEvent = ev
    }

    override fun json_event(init: IJsonEventBuilder.() -> Unit) {
        if (VERBOSE) println("@@ json_event = $init")
        val builder = JsonEventBuilder()
        builder.init()
        val ev = builder.create()
        // TODO send event
        lastJsonEvent = ev
    }

    override fun estop() {
        TODO("ConductionImpl: estop() is not yet implemented")
    }

    override fun reset_timers(vararg prefix: String) {
        TODO("ConductionImpl: reset_timers() is not yet implemented")
    }
}
