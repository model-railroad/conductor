package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.ISensor

class Sensor(override val systemName: String) : ISensor {
    private var activeInternal = false

    override val active: Boolean
        get() = activeInternal

    fun active(isActive: Boolean) {
        activeInternal = isActive
    }
}
