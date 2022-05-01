package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.dsl.IGaPageBuilder

data class GaPage(
    val url: String,
    val path: String,
    val user: String,
)

internal class GaPageBuilder : IGaPageBuilder {
    override var url: String = ""
    override var path: String = ""
    override var user: String = ""

    fun create() : GaPage = GaPage(
        url, path, user
    )
}
