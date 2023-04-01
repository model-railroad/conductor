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

import com.alflabs.conductor.util.Analytics
import com.alflabs.conductor.util.JsonSender
import com.alflabs.kv.IKeyValue
import com.alflabs.utils.IClock
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
import com.alfray.conductor.v2.script.dsl.IOnRule
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
import com.alfray.conductor.v2.script.impl.Node
import com.alfray.conductor.v2.script.impl.OnDelayRule
import com.alfray.conductor.v2.script.impl.OnRule
import com.alfray.conductor.v2.script.impl.SvgMapBuilder
import com.alfray.conductor.v2.simulator.ISimulCallback
import com.alfray.conductor.v2.utils.assertOrThrow
import javax.inject.Inject

@Script2kScope
class ConductorImpl @Inject internal constructor(
    private val clock: IClock,
    private val logger: ILogger,
    private val factory: Factory,
    private val keyValue: IKeyValue,
    private val analytics: Analytics,
    private val jsonSender: JsonSender,
    private val eStopHandler: EStopHandler,
    override val exportedVars: ExportedVars,
    private val currentContext: CurrentContext,
) : IConductor {
    private val TAG = javaClass.simpleName
    val sensors = mutableMapOf<String, ISensor>()
    val blocks = mutableMapOf<String, IBlock>()
    val turnouts = mutableMapOf<String, ITurnout>()
    val throttles = mutableMapOf<Int, IThrottle>()
    val svgMaps = mutableMapOf<String, ISvgMap>()
    val timers = mutableListOf<ITimer>()
    val rules = mutableListOf<IOnRule>()
    val activeRoutes = mutableListOf<IActiveRoute>()
    var lastGaPage: GaPage? = null
        private set
    var lastGaEvent: GaEvent? = null
        private set
    var lastJsonEvent: JsonEvent? = null
        private set
    internal val contextTimers = mutableSetOf<ExecContext>()
    private var simulCallback: ISimulCallback? = null

    override fun sensor(systemName: String): ISensor {
        return sensors.computeIfAbsent(systemName) { factory.createSensor(it) }
    }

    override fun block(systemName: String): IBlock {
        return blocks.computeIfAbsent(systemName) { factory.createBlock(it) }
    }

    override fun turnout(systemName: String): ITurnout {
        return turnouts.computeIfAbsent(systemName) { factory.createTurnout(it) }
    }

    override fun timer(delay: Delay): ITimer {
        val t = factory.createTimer(delay)
        timers.add(t)
        return t
    }

    override fun throttle(dccAddress: Int): IThrottle {
        return throttles.computeIfAbsent(dccAddress) { factory.createThrottle(it) }
    }

    override fun map(svgMapSpecification: ISvgMapBuilder.() -> Unit): ISvgMap {
        val builder = SvgMapBuilder()
        builder.svgMapSpecification()
        val m = builder.create()
        logger.assertOrThrow(TAG, !svgMaps.contains(m.name)) {
            "SvgMap ERROR: Map name ${m.name} is already defined."
        }
        svgMaps[m.name] = m
        return m
    }

    override fun on(condition: () -> Any): IOnRule {
        currentContext.assertInScriptLoader(TAG) {
            "ERROR: on..then rule must be defined at the top global level."
        }
        val rule = OnRule(condition)
        rules.add(rule)
        return rule
    }

    override fun on(delay: Delay, condition: () -> Any): IOnRule {
        val context = currentContext.assertInScriptLoader(TAG) {
            "ERROR: on..then rule must be defined at the top global level."
        }
        val rule = OnDelayRule(condition, delay, factory) { onTimer ->
            context.addTimer(onTimer)
        }
        rules.add(rule)
        return rule
    }

    override fun after(delay: Delay): IAfter {
        val context = currentContext.assertNotInScriptLoader(TAG) {
            "ERROR: after..then action must be defined in an event callback."
        }
        val after = After(delay) { afterTimer ->
            // The "After" timer only gets recorded when the "then" clause is parsed.
            context.addTimer(afterTimer)
            contextTimers.add(context)

            if (context.reason == ExecContext.Reason.NODE
                && context.parent is Node) {
                simulCallback?.onBlockTimersChanged(
                    context.parent.block.systemName,
                    context.countTimers().durationSec
                )
            }
        }
        return after
    }

    override fun activeRoute(activeRouteSpecification: IActiveRouteBuilder.() -> Unit): IActiveRoute {
        val b = ActiveRouteBuilder(clock, logger)
        b.activeRouteSpecification()
        val a = b.create(keyValue, simulCallback)
        activeRoutes.add(a)
        return a
    }

    override fun ga_page(gaPageSpecification: IGaPageBuilder.() -> Unit) {
        val builder = GaPageBuilder()
        builder.gaPageSpecification()
        val pg = builder.create()
        analytics.sendPage(pg.url, pg.path, pg.user)
        lastGaPage = pg
    }

    override fun ga_event(gaEventSpecification: IGaEventBuilder.() -> Unit) {
        val builder = GaEventBuilder()
        builder.gaEventSpecification()
        val ev = builder.create()
        analytics.sendEvent(ev.category, ev.action, ev.label, ev.user)
        lastGaEvent = ev
    }

    override fun json_event(jsonEventSpecification: IJsonEventBuilder.() -> Unit) {
        val builder = JsonEventBuilder()
        builder.jsonEventSpecification()
        val ev = builder.create()
        jsonSender.sendEvent(ev.key1, ev.key2, ev.value)
        lastJsonEvent = ev
    }

    override fun eStop() {
        logger.d(TAG, "ESTOP activated by script. All routes execution stopped until reset.")
        eStopHandler.activateEStop()
    }

    /** Internal helper to check state of registered after timers, for tests & debugging. */
    internal fun debugSumAfterTimers() : ExecContext.CountTimers {
        val ct = ExecContext.CountTimers(0, 0, 0, 0)
        contextTimers.forEach { ct.add(it.countTimers()) }
        return ct
    }

    fun setSimulCallback(simulCallback: ISimulCallback?) {
        this.simulCallback = simulCallback
    }
}

