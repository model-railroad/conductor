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

package com.alfray.dazzserv.store

import com.alflabs.dazzserv.store.DataEntry
import com.alflabs.utils.FileOps
import com.alflabs.utils.ILogger
import com.alfray.dazzserv.serv.DazzOff
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.core.util.Separators
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.annotations.VisibleForTesting
import com.google.errorprone.annotations.concurrent.GuardedBy
import java.io.File
import java.util.Deque
import java.util.concurrent.ConcurrentLinkedDeque
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-Memory Data Store for Dazz events.
 *
 * @see com.alfray.dazzserv.serv.DazzSched for the one dealing with stored files.
 */
@Suppress("FoldInitializerAndIfToElvis")
@Singleton
class DataStore @Inject constructor(
    private val logger: ILogger,
    private val fileOps: FileOps,
    private val dazzOff: DazzOff,
) {
    /// The JSON Jackson mapper helper
    private val mapper = jacksonObjectMapper()
    /// The actual "data store". Read/writes must be synchronized on "data".
    @GuardedBy(value = "data")
    private val data = mutableMapOf<String, DataEntryMap>()
    /// List of new entries not written to disk yet. Concurrent access is thread-safe.
    private val newEntries: Deque<DataEntry> = ConcurrentLinkedDeque()

    companion object {
        const val TAG = "DataStore"
        const val HISTORY_NUM_ENTRIES = 10
    }

    /**
     * Adds or update an entry.
     *
     * This is mostly useful for testing. Entries are typically added by parsing a JSON payload
     * using the [store] method, which in turn invokes this method if the payload is properly
     * decoded.
     */
    @VisibleForTesting
    internal fun add(entry: DataEntry) {
        val key = entry.key
        synchronized(data) {
            if (!data.containsKey(key)) {
                data[key] = DataEntryMap()
            }
            data[key]!!.add(entry)
        }
    }

    @VisibleForTesting
    internal fun entryToJson(entry: DataEntry): String {
        return entry.toJsonString(mapper)
    }

    @VisibleForTesting
    fun storeToJson(entries: Map<String, DataEntryMap>? = null): String {
        return mapper.writer(CustomPrettyPrinter()).writeValueAsString(entries ?: data)
    }

    fun loadFrom(file: File) {
        var n = 0
        fileOps.toString(file, Charsets.UTF_8).reader().forEachLine {
            try {
                // do not call "store" as it queues entries to be written back to save files later.
                decodeAndAddEntry(it)
                n++
            } catch (e: Exception) {
                logger.d(TAG, "[$file] Failed to decode JSON DataEntry: ${e.message}")
            }
        }
        logger.d(TAG, "Loaded $n entries from $file")
    }

    /**
     * Appends all new entries to the given file, if any.
     */
    fun saveTo(file: File) {
        if (newEntries.isEmpty()) {
            return
        }
        var n = 0
        fileOps.openFileWriter(file, /*append=*/ true).use { writer ->
            while (true) {
                val entry = newEntries.peekFirst()
                if (entry == null) {
                    break
                }
                val json = entryToJson(entry)
                writer.write("$json\n")
                n++
                // We only actually remove the entry only if the write op didn't throw.
                // This is safe since all concurrent additions only occur at the end.
                newEntries.removeFirst()
            }
        }
        logger.d(TAG, "Saved $n entries to $file")
    }

    /**
     * Keep the last N days of data and remove everything else.
     *
     * Note that "N days" does not mean "N days from now" -- we keep the N _most recent_
     * timestamps days, whatever they are.
     *
     * This assumes that the _string_ timestamps are properly formated in the expected ISO 8601
     * format (namely "1970-01-01T00:03:54Z"). Timestamps that do not follow this format are
     * simply ignored and not purged.
     */
    fun purgeOlderEntriesThan(numDaysToKeep: Int) {
        val datesToTimestamps = mutableMapOf<String, MutableList<String>>()
        var sizeBefore = 0
        synchronized(data) {
            val format = "^([0-9]{4}-[0-9]{2}-[0-9]{2}T).*".toRegex()

            // The data is { key -> { timestamp -> entry } },
            // parse all timestamps across all keys.
            data.values.forEach { dataEntryMap ->
                sizeBefore += dataEntryMap.entries.size
                dataEntryMap.entries.keys.forEach { timestamp ->
                    format.matchEntire(timestamp)?.let { match ->
                        val date = match.groupValues[1]
                        datesToTimestamps.computeIfAbsent(date) { mutableListOf() }
                        datesToTimestamps[date]!!.add(timestamp)
                    }
                }
            }
        }

        // We'll remove everything but the highest N date timestamps
        val removeDates = datesToTimestamps.keys.sortedDescending().drop(numDaysToKeep)
        if (removeDates.isNotEmpty()) {
            var sizeAfter = 0
            val removeTimestamps = mutableSetOf<String>()
            removeDates.forEach { date ->
                removeTimestamps.addAll(datesToTimestamps[date].orEmpty())
            }

            synchronized(data) {
                data.values.forEach { dataEntryMap ->
                    removeTimestamps.forEach { timestamp ->
                        dataEntryMap.entries.remove(timestamp)
                    }
                    sizeAfter += dataEntryMap.entries.size
                }
            }

            if (sizeAfter != sizeBefore) {
                logger.d(TAG, "Purged ${sizeBefore - sizeAfter} older entries")
            }
        }
    }

    /**
     * Decodes the payload, adds it to the store if valid, returns whether it was success.
     * Payload is expected to be a JSON decoding to DataEntry.
     *
     * This also queues the new entry to be automatically saved to the store files by [saveTo()].
     * Caller is the HTTP REST handler and doesn't care about error details, as any
     * error will only be detailed via the logs and not in the HTTP response.
     */
    fun store(jsonPayload: String): Boolean {
        try {
            val entry = decodeAndAddEntry(jsonPayload)
            newEntries.addLast(entry)
            return true
        } catch (e: Exception) {
            logger.d(TAG, "Failed to decode JSON DataEntry", e)
            return false
        }
    }

    private fun decodeAndAddEntry(jsonPayload: String): DataEntry {
        val entry = DataEntry.parseJson(mapper, jsonPayload)
        add(entry)
        dazzOff.monitor(entry)
        return entry
    }

    /**
     * Returns data for the key or keys denoted by the query.
     *
     * Returns an empty string on error (caller is the HTTP REST handler and doesn't care about
     * error details, they would be logged here instead.)
     */
    fun queryToJson(keyQuery: String): String {
        synchronized(data) {
            val filteredData = filterData(keyQuery)
            return storeToJson(filteredData)
        }
    }

    fun liveToJson(keyQuery: String): String {
        // Wazz Logic to serve live data:
        // - For each key, return all entries till we report one success entries.
        // - If the next one also is a success entry, report it too.
        // TBD optional: configure max num success entries via CGI param.

        synchronized(data) {
            val liveData = mutableMapOf<String, DataEntryMap>()
            val filteredData = filterData(keyQuery)

            filteredData.forEach { (key, entries) ->
                val newEntries = DataEntryMap()
                var countSuccess = 0
                for(entry in entries.entries.values) {
                    val isSuccess = entry.isState
                    if (isSuccess) {
                        countSuccess++
                    } else if (countSuccess >= 1) {
                        break
                    }
                    if (countSuccess <= 2) {
                        newEntries.add(entry)
                    } else {
                        break
                    }
                }
                liveData[key] = newEntries
            }

            return storeToJson(liveData)
        }
    }

    fun historyToJson(keyQuery: String): String {
        // Wazz Logic to serve history data:
        // - For each key, return up to 10 success entries
        // - Ignore the failed ones.
        // TBD optional: configure max num entries via CGI param.

        synchronized(data) {
            val historyData = mutableMapOf<String, DataEntryMap>()
            val filteredData = filterData(keyQuery)

            filteredData.forEach { (key, entries) ->
                val newEntries = DataEntryMap()
                for(entry in entries.entries.values) {
                    if (entry.isState) {
                        newEntries.add(entry)
                        if (newEntries.entries.size >= HISTORY_NUM_ENTRIES) {
                            break
                        }
                    }
                }
                historyData[key] = newEntries
            }

            return storeToJson(historyData)
        }
    }

    private fun filterData(keyQuery: String): Map<String, DataEntryMap> {
        val selectedKeys = mutableSetOf<String>()

        if (keyQuery.isEmpty() || keyQuery == "**") {
            // If there's no query, return the entire data store unfiltered as-is.
            // That means the caller should likely synchronize on data even when
            // manipulating the filterData result to avoid concurrent modifications.
            return data
        }

        if (keyQuery.contains("*") || keyQuery.contains("**")) {
            val keys = data.keys

            // Treat the keyQuery as a glob matcher
            // Transform the glob query into a regexp query
            // ** --> .+
            // *  --> [^/]+
            // TBD optional: regex-escape all other special characters except / ?
            val pattern = keyQuery.replace("**", ".+").replace("*", "[^/]+").toPattern()

            val filtered = keys.filter { key -> pattern.matcher(key).matches() }
            selectedKeys.addAll(filtered)
        } else {
            // Treat the keyQuery as a single key
            selectedKeys.add(keyQuery)
        }

        // Create a shallow copy of the data store with only the selected keys
        return data.filterKeys { key -> selectedKeys.contains(key) }
    }
}

/**
 * Custom pretty printer for the data store map. See CustomIndenter for details.
 *
 * This custom pretty-printer is the reason we use Jackson instead of Kotlinx Serialization.
 * In practice, this CustomPrettyPrinter will actually only be used for unit tests.
 */
private class CustomPrettyPrinter : DefaultPrettyPrinter(
    Separators.createDefaultInstance()
        .withObjectFieldValueSpacing(Separators.Spacing.AFTER)
        .withObjectEntrySpacing(Separators.Spacing.AFTER)
) {
    init {
        _arrayIndenter = NopIndenter.instance
        _objectIndenter = CustomIndenter()
    }

    override fun writeStartObject(g: JsonGenerator?) {
        super.writeStartObject(g)
    }

    override fun writeEndObject(g: JsonGenerator?, nrOfEntries: Int) {
        val ind = _objectIndenter as CustomIndenter
        ind.isEndObject = true
        super.writeEndObject(g, nrOfEntries)
        ind.isEndObject = false
    }

    override fun createInstance(): DefaultPrettyPrinter {
        return CustomPrettyPrinter()
    }
}

private class CustomIndenter : DefaultPrettyPrinter.Indenter {
    var isEndObject = false

    override fun isInline(): Boolean {
        return false
    }

    override fun writeIndentation(jg: JsonGenerator?, level: Int) {
        // Explanation of custom indentation:
        // - level 1 = root.            Uses default { EOF <content> } EOF
        // - level 2 = entry map.       Uses default { EOF <content> } EOF
        // - level 3 = key:entry.       Uses custom  { EOF <content> } no-EOF
        // - level 4 = entry content.   Uses custom  { no-EOF <content> } no-EOF
        if (level <= 2 || (!isEndObject && level <= 3)) {
            jg!!.writeRaw("\n")
            jg.writeRaw("  ".repeat(level))
        }
    }
}

