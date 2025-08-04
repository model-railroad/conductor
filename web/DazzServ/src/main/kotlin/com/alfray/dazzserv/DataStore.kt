package com.alfray.dazzserv

import com.alflabs.utils.FileOps
import com.alflabs.utils.ILogger
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.core.util.Separators
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.util.Deque
import java.util.TreeMap
import java.util.concurrent.ConcurrentLinkedDeque

@Suppress("FoldInitializerAndIfToElvis")
class DataStore(
    private val logger: ILogger,
    private val fileOps: FileOps,
) {
    /// The JSON Jackson mapper helper
    private val mapper = jacksonObjectMapper()
    /// The actual "data store". Read/writes must be synchronized on "data".
    private val data = mutableMapOf<String, DataEntryMap>()
    /// List of new entries not written to disk yet. Concurrent access is thread-safe.
    private val newEntries: Deque<DataEntry> = ConcurrentLinkedDeque()

    companion object {
        const val TAG = "DataStore"
    }

    /// Adds an entry if it's new (e.g. a key/timestamp never seen before).
    /// Already seen timestamps are ignored and not updated.
    /// Returns true if the entry was new and added, false if already seen.
    fun add(entry: DataEntry): Boolean {
        val key = entry.key
        synchronized(data) {
            if (!data.containsKey(key)) {
                data[key] = DataEntryMap()
            }
            return data[key]!!.add(entry)
        }
    }

    fun entryToJson(entry: DataEntry): String {
        return mapper.writeValueAsString(entry)
    }

    /// This method exists solely for unit tests.
    fun storeToJson(): String {
        return mapper.writer(CustomPrettyPrinter()).writeValueAsString(data)
    }

    fun loadFrom(file: File) {
        TODO()
    }

    /// Appends all new entries tro the given file, if any.
    fun saveTo(file: File) {
        if (newEntries.isEmpty()) {
            return
        }
        fileOps.openFileWriter(file, /*append=*/ true).use { writer ->
            while (true) {
                val entry = newEntries.peekFirst()
                if (entry == null) {
                    break
                }
                val json = entryToJson(entry)
                writer.write("$json\n")
                // We only actually remove the entry only if the write op didn't throw.
                // This is safe since all concurrent additions only occur at the end.
                newEntries.removeFirst()
            }
        }
    }

    /// Decodes the payload, adds it to the store if valid, returns whether it was success.
    /// Payload is expected to be a JSON decoding to DataEntry.
    /// Caller is the HTTP REST handler and doesn't care about error details, as any
    /// error will only be detailed via the logs and not in the HTTP response.
    fun store(jsonPayload: String): Boolean {
        try {
            val entry = mapper.readValue<DataEntry>(jsonPayload)
            if (add(entry)) {
                newEntries.addLast(entry)
            }
            return true
        } catch (e: Exception) {
            logger.d(TAG, "Failed to decode JSON DataEntry", e)
            return false
        }
    }
}

/// One data entry unit: key (category) -> ISO timestamp --> boolean state --> opaque payload.
data class DataEntry(
    /// The key is expected to be a path-like structure (item1/item2/.../itemN) and never empty.
    val key: String,
    /// The timestamp MUST be in ISO 8601 format: .e.g "1970-01-01T00:03:54Z"
    /// Consistency is important across all entries as string natural sorting is used to
    /// order the timestamps. This avoids having to decode the ISO timestamp in the store.
    @JsonProperty("ts") val isoTimestamp: String,
    /// The "state" is a boolean which meaning depends on the key and the application.
    /// Pretty much all the data items handled by Wazz incorporate a boolean state, although its
    /// meaning depends on the context of the data. It is thus extracted from the payload.
    @JsonProperty("st") val state: Boolean = false,
    /// The payload is an opaque string which the DataStore doesn't need to decode. It can be
    /// empty if needed. Most of the time it will be application-specific stringified JSON.
    /// Keeping it opaque means we clumsily encode a JSON String into a JSON, but OTOH it means
    /// the DataStore and the REST server does not need to be updated with the application.
    @JsonProperty("d")  val payload: String = "",
)

/// A map of all the entries for a given key. The entries are sorted by ISO timestamp (as strings).
/// There can (obviously) be only one entry per timestamp.
data class DataEntryMap(
    val entries: TreeMap<String, DataEntry> = TreeMap<String, DataEntry>()
) {
    /// Adds an entry if it's new (e.g. a timestamp never seen before).
    /// Already seen timestamps are ignored and not updated.
    /// Returns true if the entry was new and added, false if already seen.
    fun add(entry: DataEntry): Boolean {
        val ts = entry.isoTimestamp
        if (entries.containsKey(ts)) {
            return false
        }
        entries[ts] = entry
        return true
    }
}

/// Custom pretty printer for the data store map. See CustomIndenter for details.
/// This custom pretty-printer is the reason we use Jackson instead of Kotlinx Serialization.
/// In practice, this CustomPrettyPrinter will actually only be used for unit tests.
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

