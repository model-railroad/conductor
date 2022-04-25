package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.dsl.ISensor

internal class Sensor(override val systemName: String) : ISensor {
    private var _active = false

    override val active: Boolean
        get() = _active

    override fun not(): Boolean = !active

    override fun active(isActive: Boolean) {
        _active = isActive
    }
}
