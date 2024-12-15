/*
 * Project: Conductor
 * Copyright (C) 2024 alf.labs gmail com,
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

package com.alfray.conductor.v2.script.dsl

import com.alflabs.conductor.util.Analytics
import com.alflabs.utils.ILogger
import com.alfray.conductor.v2.dagger.Script2kScope
import com.alfray.conductor.v2.script.impl.GaEvent
import com.alfray.conductor.v2.script.impl.GaEventBuilder
import javax.inject.Inject

/** Conductor engine exposed access to the Google Analytics 4 Client. */
@Script2kScope
class AnalyticsPublisher @Inject internal constructor(
    private val logger: ILogger,
    private val analytics: Analytics,
) {
    private companion object {
        val TAG = AnalyticsPublisher::class.simpleName
    }
    var lastGaEvent: GaEvent? = null
        private set

    /**
     * Configures the ID for the GA server. Written by the script.
     * GA Events are not sent until this is defined.
     *
     * For GA4, format is 'GA-ID|Client-ID|AppSecret'.
     * In the parameter starts with "@", it must be the name of the file containing the ID.
     */
    fun configure(analyticsIdOrFile: String) {
        var error: String? = null
        try {
            analytics.analyticsId = analyticsIdOrFile
        } catch (e: Exception) {
            error = "Failed to read '$analyticsIdOrFile', $e"
        }
        if (analytics.analyticsId == null) {
            error = "analytics.configure(analyticId) must be defined before the first gaEvent call."
        }
        error?.let {
            logger.d(TAG, "gaTrackingId: $error")
        }
    }

    /** Sends a GA Event statistic.
     * No-op till GA ID & URL are defined. */
    fun gaEvent(gaEventSpecification: IGaEventBuilder.() -> Unit) {
        val builder = GaEventBuilder()
        builder.gaEventSpecification()
        val ev = builder.create()
        analytics.sendEvent(ev.category, ev.action, ev.label, ev.user)
        lastGaEvent = ev
    }
}
