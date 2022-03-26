package com.alfray.conductor.v2.script

interface ISensor : IActive {
    val systemName: String
}

class Sensor(override val systemName: String) : ISensor {
    private var activeInternal = false

    override val active: Boolean
        get() = activeInternal

    fun active(isActive: Boolean) {
        activeInternal = isActive
    }
}
