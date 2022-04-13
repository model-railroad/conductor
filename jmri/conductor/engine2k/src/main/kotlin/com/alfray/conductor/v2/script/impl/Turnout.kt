package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.ITurnout

class Turnout(override val systemName: String) : ITurnout {
    private var normalInternal = true

    override val normal: Boolean
        get() = normalInternal

    override val active: Boolean
        get() = normalInternal

    override fun not(): Boolean = !active

    override fun normal() {
        normalInternal = true
    }

    override fun reverse() {
        normalInternal = false
    }
}
