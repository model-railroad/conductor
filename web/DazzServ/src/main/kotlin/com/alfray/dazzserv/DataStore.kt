package com.alfray.dazzserv

import com.alflabs.utils.ILogger
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

class DataStore(
    private val logger: ILogger,
    ) {
    val data = mutableMapOf<String, DataEntryList>()

    fun add(entry: DataEntry) {
        val key = entry.key
        synchronized(data) {
            if (!data.containsKey(key)) {
                data.putIfAbsent(key, DataEntryList())
            }
            data[key]!!.add(entry)
        }
    }

    fun loadFrom(file: File) {
        TODO()
    }

    fun saveTo(file: File) {
        TODO()
    }
}

@Serializable
data class DataEntry(
    val key: String,
    @SerialName("ts")
    val isoTimestamp: String,
    @SerialName("st")
    val state: Boolean,
    @SerialName("d")
    val payload: String,
    ) {

    fun toJson(): String {
        return Json.encodeToString(this)
    }
}

@Serializable
class DataEntryList {
    val entries = mutableListOf<DataEntry>()

    fun add(entry: DataEntry) {
        entries.add(entry)
        entries.sortBy { it.isoTimestamp }
    }
}
