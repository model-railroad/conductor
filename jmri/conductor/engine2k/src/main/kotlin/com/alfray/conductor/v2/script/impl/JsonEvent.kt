package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.dsl.IJsonEventBuilder

internal data class JsonEvent(
    val key1: String,
    val key2: String,
    val value: String,
)

internal class JsonEventBuilder : IJsonEventBuilder {
    override var key1: String = ""
    override var key2: String = ""
    override var value: String = ""

    fun create() : JsonEvent = JsonEvent(
        key1, key2, value
    )
}
