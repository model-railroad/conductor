package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.IBlock

class Block(override val systemName: String) : IBlock {
    private var activeInternal = false

    override val active: Boolean
        get() = activeInternal

    override fun not(): Boolean = !active

    fun active(isActive: Boolean) {
        activeInternal = isActive
    }
}
