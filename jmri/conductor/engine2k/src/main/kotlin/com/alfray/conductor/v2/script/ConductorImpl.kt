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

import com.alflabs.conductor.util.ILocalDateTimeNowProvider
import com.alflabs.conductor.util.JsonSender
import com.alflabs.utils.ILogger
import com.alfray.conductor.v2.dagger.Script2kScope
import com.alfray.conductor.v2.script.ExecContext.Reason
import com.alfray.conductor.v2.script.dsl.AnalyticsPublisher
import com.alfray.conductor.v2.script.dsl.Delay
import com.alfray.conductor.v2.script.dsl.ExportedVars
import com.alfray.conductor.v2.script.dsl.IAfter
import com.alfray.conductor.v2.script.dsl.IBlock
import com.alfray.conductor.v2.script.dsl.IConductor
import com.alfray.conductor.v2.script.dsl.IIdleRoute
import com.alfray.conductor.v2.script.dsl.IJsonEventBuilder
import com.alfray.conductor.v2.script.dsl.IOnRule
import com.alfray.conductor.v2.script.dsl.IRoutesContainer
import com.alfray.conductor.v2.script.dsl.IRoutesContainerBuilder
import com.alfray.conductor.v2.script.dsl.ISensor
import com.alfray.conductor.v2.script.dsl.ISequenceRoute
import com.alfray.conductor.v2.script.dsl.ISvgMap
import com.alfray.conductor.v2.script.dsl.ISvgMapBuilder
import com.alfray.conductor.v2.script.dsl.IThrottle
import com.alfray.conductor.v2.script.dsl.IThrottleBuilder
import com.alfray.conductor.v2.script.dsl.ITimer
import com.alfray.conductor.v2.script.dsl.ITurnout
import com.alfray.conductor.v2.script.dsl.MqttPublisher
import com.alfray.conductor.v2.script.dsl.TCondition
import com.alfray.conductor.v2.script.impl.After
import com.alfray.conductor.v2.script.impl.Factory
import com.alfray.conductor.v2.script.impl.JsonEvent
import com.alfray.conductor.v2.script.impl.JsonEventBuilder
import com.alfray.conductor.v2.script.impl.Node
import com.alfray.conductor.v2.script.impl.OnRuleKey
import com.alfray.conductor.v2.script.impl.SvgMapBuilder
import com.alfray.conductor.v2.simulator.ISimulCallback
import com.alfray.conductor.v2.utils.assertOrThrow
import java.time.LocalDateTime
import javax.inject.Inject

@Script2kScope
class ConductorImpl @Inject internal constructor(
    private val logger: ILogger,
    private val factory: Factory,
    override val analytics: AnalyticsPublisher,
    override val mqtt: MqttPublisher,
    private val jsonSender: JsonSender,
    private val eStopHandler: EStopHandler,
    override val exportedVars: ExportedVars,
    private val currentContext: CurrentContext,
    private val localDateTimeNow: ILocalDateTimeNowProvider,
) : IConductor {
    private val TAG = javaClass.simpleName
    val sensors = mutableMapOf<String, ISensor>()
    val blocks = mutableMapOf<String, IBlock>()
    val turnouts = mutableMapOf<String, ITurnout>()
    val throttles = mutableMapOf<Int, IThrottle>()
    val svgMaps = mutableListOf<ISvgMap>()
    val timers = mutableListOf<ITimer>()
    val routesContainers = mutableListOf<IRoutesContainer>()
    var lastJsonEvent: JsonEvent? = null
        private set
    internal val contextTimers = mutableSetOf<ExecContext>()
    private var simulCallback: ISimulCallback? = null

    override fun now(): LocalDateTime {
        return localDateTimeNow.now;
    }

    override fun log(message: String) {
        logger.d("Script", message)
    }

    override fun sensor(systemName: String): ISensor {
        return sensors.computeIfAbsent(systemName) { factory.createSensor(it) }
    }

    override fun block(systemName: String): IBlock {
        return blocks.computeIfAbsent(systemName) { factory.createBlock(it) }
    }

    override fun virtualBlock(systemName: String): IBlock {
        return blocks.computeIfAbsent(systemName) { factory.createVirtualBlock(it) }
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

    override fun throttle(dccAddress: Int, throttleSpecification: IThrottleBuilder.() -> Unit): IThrottle {
        val b = factory.createThrottleBuilder()
        b.throttleSpecification()
        return throttles.computeIfAbsent(dccAddress) { factory.createThrottle(it, b) }
    }

    override fun map(svgMapSpecification: ISvgMapBuilder.() -> Unit): ISvgMap {
        val builder = SvgMapBuilder()
        builder.svgMapSpecification()
        val m = builder.create()
        svgMaps.add(m)
        // It's OK to have a map with the same name if it has a different display target.
        logger.assertOrThrow(TAG,
            svgMaps.count { it.name == m.name && it.displayOn == m.displayOn } == 1) {
            "SvgMap ERROR: Map name ${m.name} is already defined with the same display target ${m.displayOn}."
        }
        return m
    }

    override fun on(condition: TCondition): IOnRule {
        val context = currentContext.assertHasReason(TAG,
            listOf(Reason.LOAD_SCRIPT)) {
                "ERROR: on..then rule must be defined at the top global level."
            }

        val key = OnRuleKey(context, delay = null, condition)
        val rule = context.addRule(key) {
            factory.createOnRule(key)
        }
        return rule
    }

    override fun on(delay: Delay, condition: TCondition): IOnRule {
        val context =
            currentContext.assertHasReason(TAG, listOf(Reason.LOAD_SCRIPT)) {
                "ERROR: on..then rule must be defined at the top global level."
            }
        val key = OnRuleKey(context, delay, condition)
        val rule = context.addRule(key) {
            factory.createOnDelayRule(key) { onTimer ->
                context.addTimer(onTimer)
            }
        }
        return rule
    }

    override fun after(delay: Delay): IAfter {
        val context = currentContext.assertHasReason(TAG,
            listOf(
                Reason.ROUTE,
                Reason.ON_RULE,
                Reason.NODE_EVENT)) {
            "ERROR: after..then action must be defined in an event or rule definition."
        }
        val after = After(delay) { afterTimer ->
            // The "After" timer only gets recorded when the "then" clause is parsed.
            context.addTimer(afterTimer)
            contextTimers.add(context)

            if (context.reason == Reason.NODE_EVENT
                && context.parent is Node) {
                simulCallback?.onBlockTimersChanged(
                    context.parent.block.systemName,
                    context.countTimers().longestSec
                )
            }
        }
        return after
    }

    override fun routes(routesContainerSpecification: IRoutesContainerBuilder.() -> Unit): IRoutesContainer {
        val b = factory.createRoutesContainerBuilder()
        b.routesContainerSpecification()
        val a = b.create(simulCallback)
        routesContainers.add(a)
        return a
    }

    /** Finds the first route container controlling this throttle (active or not), or null. */
    fun routesContainerForThrottle(throttle: IThrottle) : IRoutesContainer? {
        for (container in routesContainers) {
            for (route in container.routes) {
                when (route) {
                    // All subtypes of IRoute
                    is ISequenceRoute -> if (route.throttle == throttle) return container
                    is IIdleRoute -> { /* no-op, an idle route has no associated throttle */ }
                }
            }
        }
        return null
    }

    override fun jsonEvent(jsonEventSpecification: IJsonEventBuilder.() -> Unit) {
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

