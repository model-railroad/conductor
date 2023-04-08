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

package com.alfray.conductor.v2.script.dsl

import com.alflabs.conductor.util.ILocalDateTimeNowProvider
import com.alflabs.kv.IKeyValue
import com.alflabs.manifest.Constants
import com.alfray.conductor.v2.dagger.Script2kScope
import java.time.LocalTime
import javax.inject.Inject

/** Variables exchanged with the Conductor engine and exported via the KV Server. */
@Script2kScope
class ExportedVars @Inject internal constructor(
    private val keyValue: IKeyValue,
    private val localDateTimeNow: ILocalDateTimeNowProvider,
) {
    /** Current time in HHMM format set by the conductor engine. Read-only. */
    val conductorTime: Int
        get() {
            // Note: This is the system time in the "default" timezone which is... well it depends.
            // Many linux installs default to UTC, so that needs to be verified on deployment site.
            val now: LocalTime = localDateTimeNow.now.toLocalTime()
            val h = now.hour
            val m = now.minute
            return h * 100 + m
        }

    /** URL to the JSON server. Written by the script.
     * The JSON server is inactive till this defined. */
    var jsonUrl: String = ""

    /** ID for the GA server. Written by the script.
     * GA Events are not sent until this is defined. */
    var gaTrackingId: String = ""

    /** Announcement text sent to the remote RTAC tablet android software.
     * Written by the script. Sent via the KV Server. */
    var rtacPsaText: String = ""

    /** Motion indication sent to the remote RTAC tablet android software.
     * Written by the script. Sent via the KV Server. */
    var rtacMotion: Boolean = false

    internal fun export() {
        keyValue.putValue(Constants.ConductorTime, conductorTime.toString(), true /*broadcast*/)
        keyValue.putValue(Constants.RtacMotion,
            if (rtacMotion) Constants.On else Constants.Off,
            true /*broadcast*/)
        keyValue.putValue(Constants.RtacPsaText, rtacPsaText, true /*broadcast*/)
        // gaTrackingId is exported by Analytics.setAnalyticsId(); we don't need to do it here.
    }
}
