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

import com.alflabs.conductor.util.FrequencyMeasurer
import com.alflabs.conductor.util.RateLimiter
import com.alflabs.kv.IKeyValue
import com.alflabs.manifest.Constants
import com.alflabs.manifest.MapInfos
import com.alflabs.utils.FileOps
import com.alflabs.utils.IClock
import com.alflabs.utils.ILogger
import com.alfray.conductor.v2.Script2kErrors
import com.alfray.conductor.v2.Script2kSource
import com.alfray.conductor.v2.dagger.Script2kScope
import com.alfray.conductor.v2.script.impl.Block
import com.alfray.conductor.v2.script.impl.IExecEngine
import com.alfray.conductor.v2.script.impl.Rule
import com.alfray.conductor.v2.script.impl.Sensor
import com.alfray.conductor.v2.script.impl.SvgMap
import com.alfray.conductor.v2.script.impl.Throttle
import com.alfray.conductor.v2.script.impl.Turnout
import com.fasterxml.jackson.core.JsonProcessingException
import java.io.IOException
import javax.inject.Inject

@Script2kScope
class ExecEngine2k @Inject constructor(
    val conductor: ConductorImpl,
    private val clock: IClock,
    private val logger: ILogger,
    private val fileOps: FileOps,
    private val keyValue: IKeyValue,
    private val eStopHandler: EStopHandler,
    private val scriptSource: Script2kSource,
    private val scriptErrors: Script2kErrors,
) : IExecEngine {
    private companion object {
        val TAG = ExecEngine2k::class.simpleName
    }

    private val handleFrequency = FrequencyMeasurer(clock)
    private val handleRateLimiter = RateLimiter(30.0f, clock)
    private val activatedRules = mutableListOf<Rule>()
    private val ruleCondCache = BooleanCache<Rule>()
    private val ruleExecCache = BooleanCache<Rule>()

    override fun onExecStart() {
        conductor.blocks.forEach { (_, block) -> (block as Block).onExecStart() }
        conductor.sensors.forEach { (_, sensor) -> (sensor as Sensor).onExecStart() }
        conductor.turnouts.forEach { (_, turnout) -> (turnout as Turnout).onExecStart() }
        conductor.throttles.forEach { (_, throttle) -> (throttle as Throttle).onExecStart() }
        reset()
        exportMaps()
        exportRoutes()
    }

    private fun exportMaps() {
        val scriptDir = scriptSource.scriptDir()
        val infos =
            conductor.svgMaps
                .map { (_, svgMap) ->
                try {
                    (svgMap as SvgMap).toMapInfo(fileOps, scriptDir)
                } catch (e: IOException) {
                    val error = "SvgMap[${svgMap.name}]: Failed to read file '${svgMap.svg}'."
                    logger.d(TAG, error, e)
                    scriptErrors.append(error)
                    return@map null
                }
            }
        val maps = MapInfos(infos.filterNotNull().toTypedArray())

        try {
            keyValue.putValue(Constants.MapsKey, maps.toJsonString(), true)
        } catch (e: JsonProcessingException) {
            logger.d(TAG, "Export KV Maps failed: $e")
        }
    }

    private fun exportRoutes() {
        // TODO("Not yet implemented")
    }

    override fun onExecHandle() {
        handleFrequency.startWork()

        propagateExecHandle()

        val eStopState: Constants.EStopState = eStopHandler.eStopState
        when (eStopState) {
            Constants.EStopState.NORMAL -> {
                evalScript()
                repeatSpeed()
            }
            Constants.EStopState.ACTIVE -> {
                if (eStopHandler.lastEStopState == Constants.EStopState.NORMAL) {
                    // First time going from NORMAL to ACTIVE E-Stop.
                    eStopAllThrottles()
                }
            }
            Constants.EStopState.RESET -> {
                reset()
            }
        }
        eStopHandler.lastEStopState = eStopState

        handleFrequency.endWork()
        handleRateLimiter.limit()
    }

    private fun propagateExecHandle() {
        conductor.blocks.forEach { (_, block) -> (block as Block).onExecHandle() }
        conductor.sensors.forEach { (_, sensor) -> (sensor as Sensor).onExecHandle() }
        conductor.turnouts.forEach { (_, turnout) -> (turnout as Turnout).onExecHandle() }
        conductor.throttles.forEach { (_, throttle) -> (throttle as Throttle).onExecHandle() }
    }

    private fun repeatSpeed() {
        conductor.throttles.forEach { (_, throttle) -> (throttle as Throttle).repeatSpeed() }
    }

    private fun eStopAllThrottles() {
        conductor.throttles.forEach { (_, throttle) -> (throttle as Throttle).eStop() }
    }

    private fun reset() {
        // Make these objects resettable on a per-need basis.
        // conductor.blocks.forEach { (_, block) -> (block as Block).reset() }
        // conductor.sensors.forEach { (_, sensor) -> (sensor as Sensor).reset() }
        // conductor.turnouts.forEach { (_, turnout) -> (turnout as Turnout).reset() }
        // conductor.throttles.forEach { (_, throttle) -> (throttle as Throttle).reset() }
        ruleCondCache.clear()
        activatedRules.clear()
        eStopHandler.reset()
    }

    private fun evalScript() {
        ruleCondCache.clear()
        activatedRules.clear()

        // First collect all rules with an active condition that have not been
        // executed yet.
        for (r in conductor.rules) {
            val rule = r as Rule
            val active = ruleCondCache.getOrEval(rule) {
                var result = false
                try {
                    result = rule.evaluateCondition()
                } catch (t: Throwable) {
                    logger.d(TAG, "Eval Condition Failed", t)
                }
                result
            }

            // Rules only get executed once when activated and until
            // the condition is cleared and activated again.
            if (active) {
                if (!ruleExecCache.get(rule)) {
                    activatedRules.add(rule)
                }
            } else {
                ruleExecCache.remove(rule)
            }
        }

        // TBD also add rules from any currently active route, in order.

        // Second execute all actions in the order they are queued.
        for (rule in activatedRules) {
            try {
                ruleExecCache.put(rule, true)
                rule.evaluateAction()
            } catch (t: Throwable) {
                logger.d(TAG, "Eval Action Failed", t)
            }
        }
    }

    fun getActualFrequency(): Float {
        return handleFrequency.actualFrequency
    }

    fun getMaxFrequency(): Float {
        return handleFrequency.maxFrequency
    }

}
