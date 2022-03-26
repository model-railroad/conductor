package com.alfray.conductor.v2.script

interface ITurnout : IActive {
    val systemName: String
    val normal: Boolean
    fun normal()
    fun reverse()
}

class Turnout(override val systemName: String) : ITurnout {
    private var normalInternal = true

    override val normal: Boolean
        get() = normalInternal

    override val active: Boolean
        get() = normalInternal

    override fun normal() {
        normalInternal = true
    }

    override fun reverse() {
        normalInternal = false
    }
}
