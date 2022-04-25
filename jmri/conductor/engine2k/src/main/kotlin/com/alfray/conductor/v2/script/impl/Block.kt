package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.dsl.IBlock

internal class Block(override val systemName: String) : IBlock {
    private var _active = false

    override val active: Boolean
        get() = _active

    override fun not(): Boolean = !active

    fun active(isActive: Boolean) {
        _active = isActive
    }
}
