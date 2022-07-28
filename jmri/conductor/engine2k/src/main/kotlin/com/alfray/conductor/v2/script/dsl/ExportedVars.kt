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

import com.alflabs.kv.IKeyValue
import com.alflabs.manifest.Constants
import com.alfray.conductor.v2.dagger.Script2kScope
import javax.inject.Inject

/** Variables exchanged with the Conductor engine and exported via the KV Server. */
@Script2kScope
class ExportedVars @Inject internal constructor(
    private val keyValue: IKeyValue,
) {
    /** Current time in HHMM format set by the conductor engine. Read-only. */
    var Conductor_Time: Int = 0

    /** URL to the JSON server. Written by the script.
     * The JSON server is inactive till this defined. */
    var JSON_URL: String = ""

    /** ID for the GA server. Written by the script.
     * GA Events are not sent until this is defined. */
    var GA_Tracking_Id: String = ""

    /** Site URL for the GA server. Written by the script.
     * GA Events are not sent until this is defined. */
    var GA_URL: String = ""

    /** Announcement text sent to the remote RTAC tablet android software.
     * Written by the script. Sent via the KV Server. */
    var RTAC_PSA_Text: String = ""

    /** Motion indication sent to the remote RTAC tablet android software.
     * Written by the script. Sent via the KV Server. */
    var RTAC_Motion: Boolean = false

    internal fun export() {
        keyValue.putValue(Constants.ConductorTime, Conductor_Time.toString(), true /*broadcast*/)
        keyValue.putValue(Constants.GAId, GA_Tracking_Id, true /*broadcast*/)
        keyValue.putValue(Constants.RtacPsaText, RTAC_PSA_Text, true /*broadcast*/)
        keyValue.putValue(Constants.RtacMotion,
            if (RTAC_Motion) Constants.On else Constants.Off,
            true /*broadcast*/)
    }
}
