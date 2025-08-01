package com.alfray.dazzserv

import com.alflabs.utils.ILogger
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.core.util.Separators
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.util.TreeMap

class DataStore(
    private val logger: ILogger,
) {
    val data = mutableMapOf<String, DataEntryMap>()
    val mapper = jacksonObjectMapper()

    fun add(entry: DataEntry) {
        val key = entry.key
        synchronized(data) {
            if (!data.containsKey(key)) {
                data.putIfAbsent(key, DataEntryMap())
            }
            data[key]!!.add(entry)
        }
    }

    fun entryToJson(entry: DataEntry): String {
        return mapper.writeValueAsString(entry)
    }

    fun storeToJson(): String {
        return mapper.writer(CustomPrettyPrinter()).writeValueAsString(data)
    }

    fun loadFrom(file: File) {
        TODO()
    }

    fun saveTo(file: File) {
        TODO()
    }
}

data class DataEntry(
    val key: String,
    @JsonProperty("ts") val isoTimestamp: String,
    @JsonProperty("st") val state: Boolean,
    @JsonProperty("d")  val payload: String,
)

data class DataEntryMap(
    val entries: TreeMap<String, DataEntry> = TreeMap<String, DataEntry>()
) {
    fun add(entry: DataEntry) {
        entries[entry.isoTimestamp] = entry
    }
}

/// Custom pretty printer for the data store map. See CustomIndenter for details.
/// This custom pretty-printer is the reason we use Jackson instead of Kotlinx Serialization.
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

