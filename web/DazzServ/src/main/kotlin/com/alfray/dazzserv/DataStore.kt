package com.alfray.dazzserv

import com.alflabs.utils.ILogger
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.elementDescriptors
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.io.File
import java.util.TreeMap

class DataStore(
    private val logger: ILogger,
) {
    val data = mutableMapOf<String, DataEntryList>()

    @OptIn(ExperimentalSerializationApi::class)
    val jsonFormat = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    }

    fun add(entry: DataEntry) {
        val key = entry.key
        synchronized(data) {
            if (!data.containsKey(key)) {
                data.putIfAbsent(key, DataEntryList())
            }
            data[key]!!.add(entry)
        }
    }

    internal fun toJson(): String {
        return jsonFormat.encodeToString(data)
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
    @Serializable(with = TreeMapSerializer::class)
    private val entries = TreeMap<String, DataEntry>()

    fun add(entry: DataEntry) {
        entries[entry.isoTimestamp] = entry
    }
}

internal object TreeMapSerializer : KSerializer<TreeMap<String, DataEntry>> {
    private val delegateSerializer = MapSerializer(String.serializer(), DataEntry.serializer())

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("TreeMap") {
        delegateSerializer.descriptor.elementDescriptors.forEach {
            element(it.serialName, it)
        }
    }

    override fun serialize(encoder: Encoder, value: TreeMap<String, DataEntry>) {
        delegateSerializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): TreeMap<String, DataEntry> {
        return TreeMap(delegateSerializer.deserialize(decoder))
    }
}
