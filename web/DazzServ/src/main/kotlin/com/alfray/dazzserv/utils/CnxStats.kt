/*
 * Project: DazzServ
 * Copyright (C) 2025 alf.labs gmail com,
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

package com.alfray.dazzserv.utils

import com.alflabs.utils.ILogger
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CnxStats @Inject constructor(
    private val logger: ILogger,
) {
    private val dataMap = ConcurrentHashMap<String, CnxStatsData>()

    companion object {
        const val TAG = "CnxStats"
    }

    fun accumulate(label: String, bytesIn: Long, bytesOut: Long) {
        dataMap.computeIfAbsent(label) { CnxStatsData() }
        dataMap[label]?.let { data ->
            synchronized(data) {
                data.numRequests++
                data.sumBytesIn += bytesIn
                data.sumBytesOut += bytesOut
            }
            data.log(logger, label)
        }
    }

    fun log() {
        dataMap.forEach { (label, data) ->
            data.log(logger, label)
        }
    }

    private data class CnxStatsData(
        var numRequests: Int = 0,
        var sumBytesIn: Long = 0L,
        var sumBytesOut: Long = 0L,
    ) {
        fun log(logger: ILogger, label: String) {
            logger.d(TAG, "[$label] $numRequests requests, $sumBytesIn bytes in, $sumBytesOut bytes out")
        }
    }
}
