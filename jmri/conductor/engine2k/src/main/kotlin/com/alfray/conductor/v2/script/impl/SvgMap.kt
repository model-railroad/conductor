package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.ISvgMap

class SvgMap(override val name: String, override val svg: String) : ISvgMap {

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
