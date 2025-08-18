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

package com.alfray.conductor.v2.script.impl

import com.alflabs.conductor.jmri.IJmriProvider
import com.alflabs.conductor.util.DazzSender
import com.alflabs.conductor.util.EventLogger
import com.alflabs.conductor.util.JsonSender
import com.alflabs.kv.IKeyValue
import com.alflabs.utils.IClock
import com.alflabs.utils.ILogger
import com.alfray.conductor.v2.dagger.Script2kIsSimulation
import com.alfray.conductor.v2.dagger.Script2kScope
import com.alfray.conductor.v2.script.CondCache
import com.alfray.conductor.v2.script.CurrentContext
import com.alfray.conductor.v2.script.dsl.Delay
import com.alfray.conductor.v2.script.dsl.IBlock
import com.alfray.conductor.v2.script.dsl.IRoutesContainer
import com.alfray.conductor.v2.simulator.ISimulCallback
import java.text.DateFormat
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider

@Script2kScope
internal class Factory @Inject constructor(
    private val clock: IClock,
    private val logger: ILogger,
    private val keyValue: IKeyValue,
    private val condCache: CondCache,
    private val jsonSender: JsonSender,
    private val dazzSender: DazzSender,
    private val eventLogger: EventLogger,
    private val jmriProvider: IJmriProvider,
    private val currentContext: CurrentContext,
    private val isSimulation: Script2kIsSimulation,
    private val throttleBuilderProvider: Provider<ThrottleBuilder>,
    @Named("IsoUtcDateTime") private val jsonDateFormat: DateFormat,
    private val routesContainerBuilderProvider: Provider<RoutesContainerBuilder>,
) {
    internal fun createBlock(
            systemName: String) : Block =
        Block(keyValue, jmriProvider, clock, condCache, eventLogger, systemName)

    internal fun createIdleRoute(owner: IRoutesContainer, builder: IdleRouteBuilder): IdleRoute =
        IdleRoute(logger, eventLogger, owner, builder)

    internal fun createIdleRouteBuilder(owner: IRoutesContainer): IdleRouteBuilder =
        IdleRouteBuilder(logger, this, owner)

    internal fun createNodeBuilder(block: IBlock): NodeBuilder =
        NodeBuilder(logger, block)

    internal fun createOnDelayRule(key: OnRuleKey, registerTimer: (OnDelayRule) -> Unit): OnDelayRule =
        OnDelayRule(this, key, registerTimer)

    internal fun createOnRule(key: OnRuleKey): OnRule =
        OnRule(key)

    internal fun createRoutesContainer(
            simulCallback: ISimulCallback?,
            builder: RoutesContainerBuilder) : RoutesContainer =
        RoutesContainer(logger, this, keyValue, simulCallback, builder)

    internal fun createRoutesContainerBuilder(): RoutesContainerBuilder =
        routesContainerBuilderProvider.get()

    internal fun createRouteGraphBuilder(): RouteGraphBuilder =
        RouteGraphBuilder(logger)

    internal fun createSensor(systemName: String) : Sensor =
        Sensor(keyValue, condCache, eventLogger, jmriProvider, systemName)

    internal fun createSequenceRoute(owner: IRoutesContainer, builder: SequenceRouteBuilder): SequenceRoute =
        SequenceRoute(clock, logger, this, jsonSender, dazzSender, eventLogger, owner, builder)

    internal fun createSequenceRouteBuilder(owner: IRoutesContainer): SequenceRouteBuilder =
        SequenceRouteBuilder(logger, this, owner)

    internal fun createSequenceRouteStats(routeName: String, throttleName: String): SequenceRouteStats =
        SequenceRouteStats(clock, jsonDateFormat, routeName, throttleName)

    internal fun createTimer(delay: Delay) : Timer =
        Timer(clock, logger, eventLogger, delay)

    internal fun createThrottle(
            dccAddress: Int,
            builder: ThrottleBuilder? = null) : Throttle =
        Throttle(clock, logger, keyValue, condCache, eventLogger, jmriProvider, currentContext, dccAddress, builder)

    internal fun createThrottleBuilder(): ThrottleBuilder =
        throttleBuilderProvider.get()

    internal fun createTurnout(systemName: String) : Turnout =
        Turnout(keyValue, condCache, eventLogger, jmriProvider, systemName)

    internal fun createVirtualBlock(systemName: String) : VirtualBlock =
        VirtualBlock(keyValue, clock, condCache, eventLogger, systemName)
}

