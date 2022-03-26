package com.alfray.conductor.v2.script

interface IBlock : IActive {
    val systemName: String
}

class Block(override val systemName: String) : IBlock {
    private var activeInternal = false

    override val active: Boolean
        get() = activeInternal

    fun active(isActive: Boolean) {
        activeInternal = isActive
    }
}
