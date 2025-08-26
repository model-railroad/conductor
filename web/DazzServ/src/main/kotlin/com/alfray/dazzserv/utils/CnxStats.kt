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

import com.alflabs.utils.FileOps
import com.alflabs.utils.IClock
import com.alflabs.utils.ILogger
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
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
    private val fileOps: FileOps,
    @Named("IsoDateOnly") private val dayDateFormat: DateFormat,
    @Named("IsoYearMonth") private val monthDateFormat: DateFormat,
) {
    private val store = CnxStatsStore()

    companion object {
        const val TAG = "CnxStats"
        const val FILENAME = "cnxstats.json"
        const val NUM_DAYS = 7
        const val NUM_MONTHS = 12
    }

    private fun getCurrentDayStats(nowTS: Long): CnxStatsMap {
        val day = dayDateFormat.format(Date(nowTS))

        synchronized(store.daysMap) {
            store.daysMap.computeIfAbsent(day) { CnxStatsMap(date = day) }

            while (store.daysMap.size > NUM_DAYS) {
                store.daysMap.keys.toList().minOf { it }.let { oldest ->
                    logger.d(TAG, "Discard stats for day $oldest")
                    store.daysMap[oldest]?.log(logger)
                    store.daysMap.remove(oldest)
                }
            }

            return store.daysMap[day]!!
        }
    }

    private fun getCurrentMonthStats(nowTS: Long): CnxStatsMap {
        val month = monthDateFormat.format(Date(nowTS))

        synchronized(store.monthsMap) {
            store.monthsMap.computeIfAbsent(month) { CnxStatsMap(date = month) }

            while (store.monthsMap.size > NUM_MONTHS) {
                store.monthsMap.keys.toList().minOf { it }.let { oldest ->
                    logger.d(TAG, "Discard stats for month $oldest")
                    store.monthsMap[oldest]?.log(logger)
                    store.monthsMap.remove(oldest)
                }
            }

            return store.monthsMap[month]!!
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
        synchronized(store.daysMap) {
            store.daysMap.forEach { (_, map) -> map.log(logger) }
        }
    }

    fun logMonths() {
        synchronized(store.monthsMap) {
            store.monthsMap.forEach { (_, map) -> map.log(logger) }
        }
    }

    fun logToString(): String {
        val builder = StringBuilder()

        builder.append("Days:\n")
        synchronized(store.daysMap) {
            store.daysMap.forEach { (_, map) ->
                map.logToStrings().forEach { str ->
                    builder.append(str).append('\n')
                }
            }
        }
        builder.append("\n")

        builder.append("Months:\n")
        synchronized(store.monthsMap) {
            store.monthsMap.forEach { (_, map) ->
                map.logToStrings().forEach { str ->
                    builder.append(str).append('\n')
                }
            }
        }
        builder.append("\n")

        return builder.toString()
    }

    fun load(storeDir: String) {
        try {
            val mapper = jacksonObjectMapper()
            val file = getStoreFile(storeDir)
            if (file == null) {
                logger.d(TAG, "Load, ignoring invalid $storeDir")
                return
            }
            if (!fileOps.isFile(file)) {
                logger.d(TAG, "Load, ignoring invalid $file")
                return
            }
            val content = fileOps.toString(file, Charsets.UTF_8)
            val newStore = mapper.readValue(content, CnxStatsStore::class.java)
            synchronized(store.daysMap) {
                store.daysMap.clear()
                store.daysMap.putAll(newStore.daysMap)
            }
            synchronized(store.monthsMap) {
                store.monthsMap.clear()
                store.monthsMap.putAll(newStore.monthsMap)
            }
            logger.d(TAG, "Loaded $file")
        } catch (e: Exception) {
            // IO exception or JSON serialization exception
            logger.d(TAG, "Load error from $storeDir", e)
        }
    }

    fun save(storeDir: String) {
        try {
            val mapper = jacksonObjectMapper().writerWithDefaultPrettyPrinter()
            val file = getStoreFile(storeDir)
            if (file == null) {
                logger.d(TAG, "Load, ignoring invalid $storeDir")
            }
            val bytes = mapper.writeValueAsBytes(store)
            fileOps.writeBytes(bytes, file)
            logger.d(TAG, "Stored $file")
        } catch (e: Exception) {
            // IO exception or JSON serialization exception
            logger.d(TAG, "Save error to $storeDir", e)
        }
    }

    private fun getStoreFile(storeDir: String): File? {
        var storeDirF = File(storeDir)
        if (storeDir.startsWith("~") && !fileOps.isDir(storeDirF) && !fileOps.isFile(storeDirF)) {
            storeDirF = File(System.getProperty("user.home"), storeDir.substring(1))
        }
        return if (fileOps.isDir(storeDirF)) File(storeDirF, FILENAME) else null

    }

    private data class CnxStatsStore(
        /** An ISO day string (e.g. "2025-08-01" to a CnxStatsMap. */
        val daysMap : TreeMap<String, CnxStatsMap> = TreeMap(),
        /** An ISO month string (e.g. "2025-08" to a CnxStatsMap. */
        val monthsMap : TreeMap<String, CnxStatsMap> = TreeMap(),
    )

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
