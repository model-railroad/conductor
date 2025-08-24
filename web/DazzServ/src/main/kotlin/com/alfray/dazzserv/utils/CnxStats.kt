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

import com.alflabs.utils.IClock
import com.alflabs.utils.ILogger
import java.lang.StringBuilder
import java.text.DateFormat
import java.util.Date
import java.util.TreeMap
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class CnxStats @Inject constructor(
    private val logger: ILogger,
    private val clock: IClock,
    @Named("IsoDateOnly") private val dayDateFormat: DateFormat,
    @Named("IsoYearMonth") private val monthDateFormat: DateFormat,
) {
    /** An ISO day string (e.g. "2025-08-01" to a CnxStatsMap. */
    private val daysMap = TreeMap<String, CnxStatsMap>()
    /** An ISO month string (e.g. "2025-08" to a CnxStatsMap. */
    private val monthsMap = TreeMap<String, CnxStatsMap>()


    companion object {
        const val TAG = "CnxStats"
        const val NUM_DAYS = 7
        const val NUM_MONTHS = 12
    }

    private fun getCurrentDayStats(nowTS: Long): CnxStatsMap {
        val day = dayDateFormat.format(Date(nowTS))

        synchronized(daysMap) {
            daysMap.computeIfAbsent(day) { CnxStatsMap(date = day) }

            while (daysMap.size > NUM_DAYS) {
                daysMap.keys.toList().minOf { it }.let { oldest ->
                    logger.d(TAG, "Discard stats for day $oldest")
                    daysMap[oldest]?.log(logger)
                    daysMap.remove(oldest)
                }
            }

            return daysMap[day]!!
        }
    }

    private fun getCurrentMonthStats(nowTS: Long): CnxStatsMap {
        val month = monthDateFormat.format(Date(nowTS))

        synchronized(monthsMap) {
            monthsMap.computeIfAbsent(month) { CnxStatsMap(date = month) }

            while (monthsMap.size > NUM_MONTHS) {
                monthsMap.keys.toList().minOf { it }.let { oldest ->
                    logger.d(TAG, "Discard stats for month $oldest")
                    monthsMap[oldest]?.log(logger)
                    monthsMap.remove(oldest)
                }
            }

            return monthsMap[month]!!
        }
    }

    fun accumulate(label: String, bytesIn: Long, bytesOut: Long) {
        val nowTS = clock.elapsedRealtime()
        accumulateInMap(getCurrentDayStats(nowTS), label, bytesIn, bytesOut, log = true)
        accumulateInMap(getCurrentMonthStats(nowTS), label, bytesIn, bytesOut)
    }

    private fun accumulateInMap(
        dataMap: CnxStatsMap,
        label: String,
        bytesIn: Long,
        bytesOut: Long,
        log: Boolean = false,
    ) {
        synchronized(dataMap) {
            dataMap.map.computeIfAbsent(label) { CnxStatsData() }
            dataMap.map[label]?.let { data ->
                data.numRequests++
                data.sumBytesIn += bytesIn
                data.sumBytesOut += bytesOut
                if (log) {
                    data.log(logger, dataMap.date, label)
                }
            }
        }
    }

    fun logDays() {
        synchronized(daysMap) {
            daysMap.forEach { (_, map) -> map.log(logger) }
        }
    }

    fun logMonths() {
        synchronized(monthsMap) {
            monthsMap.forEach { (_, map) -> map.log(logger) }
        }
    }

    fun logToString(): String {
        val builder = StringBuilder()

        builder.append("Days:\n\n")
        synchronized(daysMap) {
            daysMap.forEach { (_, map) ->
                map.logToStrings().forEach { str ->
                    builder.append(str).append('\n')
                }
                builder.append('\n')
            }
        }

        builder.append("Months:\n\n")
        synchronized(monthsMap) {
            monthsMap.forEach { (_, map) ->
                map.logToStrings().forEach { str ->
                    builder.append(str).append('\n')
                }
                builder.append('\n')
            }
        }

        return builder.toString()
    }

    private data class CnxStatsData(
        var numRequests: Int = 0,
        var sumBytesIn: Long = 0L,
        var sumBytesOut: Long = 0L,
    ) {
        fun log(logger: ILogger, date: String, label: String) {
            logger.d(TAG, "$date [${label.padEnd(5)}] $numRequests requests, $sumBytesIn bytes in, $sumBytesOut bytes out")
        }

        fun logToString(date: String, label: String): String {
            return "$date [${label.padEnd(5)}] $numRequests requests, $sumBytesIn bytes in, $sumBytesOut bytes out"
        }
    }

    private data class CnxStatsMap(
        /// The current day or month of this accumulation map.
        val date: String,
        /// A map of request labels to their request stats.
        val map: TreeMap<String, CnxStatsData> = TreeMap()
    ) {
        fun log(logger: ILogger) {
            synchronized(this) {
                map.forEach { (label, data) ->
                    data.log(logger, date, label)
                }
            }
        }

        fun logToStrings(): List<String> {
            synchronized(this) {
                return map.map( { (label, data) ->
                    data.logToString(date, label)
                })
            }
        }
    }
}
