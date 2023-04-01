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
import com.alflabs.conductor.util.FrequencyMeasurer
import com.alflabs.conductor.util.JsonSender
import com.alflabs.conductor.util.RateLimiter
import com.alflabs.kv.IKeyValue
import com.alflabs.manifest.Constants
import com.alflabs.manifest.MapInfos
import com.alflabs.manifest.RouteInfos
import com.alflabs.utils.FileOps
import com.alflabs.utils.IClock
import com.alflabs.utils.ILogger
import com.alfray.conductor.v2.Script2kErrors
import com.alfray.conductor.v2.Script2kSource
import com.alfray.conductor.v2.dagger.Script2kScope
import com.alfray.conductor.v2.script.dsl.IOnRule
import com.alfray.conductor.v2.script.dsl.TAction
import com.alfray.conductor.v2.script.impl.ActiveRoute
import com.alfray.conductor.v2.script.impl.After
import com.alfray.conductor.v2.script.impl.Block
import com.alfray.conductor.v2.script.impl.Factory
import com.alfray.conductor.v2.script.impl.IExecEngine
import com.alfray.conductor.v2.script.impl.RouteSequence
import com.alfray.conductor.v2.script.impl.OnRule
import com.alfray.conductor.v2.script.impl.Sensor
import com.alfray.conductor.v2.script.impl.SvgMap
import com.alfray.conductor.v2.script.impl.Throttle
import com.alfray.conductor.v2.script.impl.Turnout
import com.alfray.conductor.v2.utils.BooleanCache
import com.fasterxml.jackson.core.JsonProcessingException
import java.io.IOException
import java.lang.Exception
import javax.inject.Inject

@Script2kScope
class ExecEngine2k @Inject internal constructor(
    val conductor: ConductorImpl,
    private val clock: IClock,
    private val logger: ILogger,
    private val factory: Factory,
    private val fileOps: FileOps,
    private val keyValue: IKeyValue,
    private val condCache: CondCache,
    private val analytics: Analytics,
    private val jsonSender: JsonSender,
    private val eStopHandler: EStopHandler,
    private val scriptSource: Script2kSource,
    private val scriptErrors: Script2kErrors,
    private val currentContext: CurrentContext,
) : IExecEngine {
    private companion object {
        val TAG = ExecEngine2k::class.simpleName
    }

    private val handleFrequency = FrequencyMeasurer(clock)
    private val handleRateLimiter = RateLimiter(30.0f, clock)
    private val activatedActions = mutableListOf<ExecAction>()
    private val actionExecCache = BooleanCache<TAction>()
    private val globalRuleContext = ExecContext(ExecContext.Reason.GLOBAL_RULE)

    override fun onExecStart() {
        initFromExportedVars()
        conductor.blocks.forEach { (_, block) -> (block as Block).onExecStart() }
        conductor.sensors.forEach { (_, sensor) -> (sensor as Sensor).onExecStart() }
        conductor.turnouts.forEach { (_, turnout) -> (turnout as Turnout).onExecStart() }
        conductor.throttles.forEach { (_, throttle) -> (throttle as Throttle).onExecStart() }
        // Routes must be started after all sensor objects.
        conductor.activeRoutes.forEach { (it as ActiveRoute).onExecStart() }
        reset()
        exportMaps()
        exportRoutes()
    }

    private fun initFromExportedVars() {
        configureAnalyticsId()
        configureJsonSenderUrl()
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
                    scriptErrors.add(error)
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
        conductor.activeRoutes.forEach { ar ->
            ar.routes.forEach {
                if (it is RouteSequence) {
                    logger.d(TAG, "Active Route [${ar.name}]: $it = ${it.graph}")
                } else {
                    logger.d(TAG, "Active Route [${ar.name}]: $it")
                }
            }
        }

        val infos = RouteInfos(
            conductor.activeRoutes.map { (it as ActiveRoute).routeInfo }.toTypedArray())

        try {
            keyValue.putValue(Constants.RoutesKey, infos.toJsonString(), true)
        } catch (e: JsonProcessingException) {
            logger.d(TAG, "Export Route Infos failed: $e")
        }
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
        conductor.exportedVars.export()
        eStopHandler.lastEStopState = eStopState

        handleFrequency.endWork()
        handleRateLimiter.limit()
    }

    private fun propagateExecHandle() {
        conductor.blocks.forEach { (_, block) -> (block as Block).onExecHandle() }
        conductor.sensors.forEach { (_, sensor) -> (sensor as Sensor).onExecHandle() }
        conductor.turnouts.forEach { (_, turnout) -> (turnout as Turnout).onExecHandle() }
        conductor.throttles.forEach { (_, throttle) -> (throttle as Throttle).onExecHandle() }
        conductor.activeRoutes.forEach { (it as ActiveRoute).onExecHandle() }
    }

    private fun repeatSpeed() {
        currentContext.changeContext(globalRuleContext)
        try {
            conductor.throttles.forEach { (_, throttle) -> (throttle as Throttle).repeatSpeed() }
        } finally {
            currentContext.resetContext()
        }
    }

    private fun eStopAllThrottles() {
        currentContext.changeContext(globalRuleContext)
        try {
            conductor.throttles.forEach { (_, throttle) -> (throttle as Throttle).eStop() }
        } finally {
            currentContext.resetContext()
        }
    }

    private fun reset() {
        // Make these objects resettable on a per-need basis.
        // conductor.blocks.forEach { (_, block) -> (block as Block).reset() }
        // conductor.sensors.forEach { (_, sensor) -> (sensor as Sensor).reset() }
        // conductor.turnouts.forEach { (_, turnout) -> (turnout as Turnout).reset() }
        // conductor.throttles.forEach { (_, throttle) -> (throttle as Throttle).reset() }
        condCache.unfreeze()
        activatedActions.clear()
        eStopHandler.reset()
    }

    private fun evalScript() {
        condCache.freeze()
        activatedActions.clear()

        // Collect all rules with an active condition that have not been executed yet.
        conductor.rules.forEach { collectOnRuleAction(it) }

        // Add rules from any currently active route, in order.
        conductor.activeRoutes.forEach { a ->
            a as ActiveRoute
            a.collectActions(activatedActions)
        }

        // Process all after timers
        // Note: on-delay timers are evaluated via collectOnRuleAction()
        conductor.contextTimers.forEach { c ->
            c.evalAfterTimers(this::collectAfterAction)
        }

        // Execute all actions in the order they are queued.
        for ((context, action) in activatedActions) {
            currentContext.changeContext(context)
            // Remember that this action has been executed. collectRuleAction() will later omit
            // it unless the condition becomes false in between.
            actionExecCache.put(action, true)
            try {
                action.invoke()
            } catch (t: Throwable) {
                val stackTrace = t.stackTraceToString()
                logger.d(TAG, "Eval action failed: $t\n$stackTrace")
            }
        }
        currentContext.resetContext()
        condCache.unfreeze()
    }

    private fun collectOnRuleAction(r: IOnRule) {
        val rule = r as OnRule
        val active: Boolean
        try {
            active = rule.evaluateCondition()
        } catch (t: Throwable) {
            logger.d(TAG, "Eval rule condition failed", t)
            return
        }

        // Non-delay Rules only get executed once when activated and until
        // the condition is cleared and activated again.
        val action: ExecAction
        try {
            action = rule.getAction()
        } catch (t: Throwable) {
            logger.d(TAG, "Eval rule action failed", t)
            return
        }

        if (active) {
            if (!actionExecCache.get(action.action)) {
                activatedActions.add(action)
            }
        } else {
            rule.clearTimers()
            actionExecCache.remove(action.action)
        }
    }

    private fun collectAfterAction(context: ExecContext, after: After) {
        val action = after.eval(logger, factory)
        action?.let {
            activatedActions.add(ExecAction(context, action))
        }
    }

    fun getActualFrequency(): Float {
        return handleFrequency.actualFrequency
    }

    fun getMaxFrequency(): Float {
        return handleFrequency.maxFrequency
    }

    /** Configure the JSON Sender URL from ExportedVars. */
    private fun configureJsonSenderUrl() {
        var error: String? = null
        val urlOrFile = conductor.exportedVars.JSON_URL
        try {
            jsonSender.setJsonUrl(urlOrFile)
        } catch (e: Exception) {
            error = "Failed to read '$urlOrFile', $e"
        }
        if (jsonSender.jsonUrl == null) {
            error = "exportedVars.JSON_URL must be defined before the first jsonEvent call."
        }
        error?.let {
            logger.d(TAG, "JSON_URL: $error")
        }
    }

    /** Configure the Analytics ID from ExportedVars. */
    private fun configureAnalyticsId() {
        var error: String? = null
        val idOrFile = conductor.exportedVars.GA_Tracking_Id
        try {
            analytics.analyticsId = idOrFile
        } catch (e: Exception) {
            error = "Failed to read '$idOrFile', $e"
        }
        if (analytics.analyticsId == null) {
            error = "exportedVars.GA_Tracking_Id must be defined before the first gaPage/gaEvent call."
        }
        error?.let {
            logger.d(TAG, "GA_Tracking_Id: $error")
        }
    }
}

