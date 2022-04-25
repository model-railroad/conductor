package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.dsl.IGaEventBuilder

internal data class GaEvent(
    val category: String,
    val action: String,
    val label: String,
    val user: String,
)

internal class GaEventBuilder : IGaEventBuilder {
    override var category: String = ""
    override var action: String = ""
    override var label: String = ""
    override var user: String = ""

    fun create() : GaEvent = GaEvent(
        category, action, label, user
    )
}
