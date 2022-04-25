package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.dsl.ISvgMap
import com.alfray.conductor.v2.script.dsl.ISvgMapBuilder

internal class SvgMapBuilder constructor() : ISvgMapBuilder {
    override lateinit var name: String
    override lateinit var svg: String

    constructor(name: String, svg: String) : this() {
        this.name = name
        this.svg = svg
    }

    fun create() : ISvgMap = SvgMap(this)
}

internal class SvgMap(builder: ISvgMapBuilder) : ISvgMap {
    override val name = builder.name
    override val svg = builder.svg

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SvgMap

        if (name != other.name) return false
        if (svg != other.svg) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + svg.hashCode()
        return result
    }
}
