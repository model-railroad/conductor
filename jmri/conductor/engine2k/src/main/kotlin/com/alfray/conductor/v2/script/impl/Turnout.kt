package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.ITurnout

class Turnout(override val systemName: String) : ITurnout {
    private var _normal = true

    override val normal: Boolean
        get() = _normal

    override val active: Boolean
        get() = _normal

    override fun not(): Boolean = !active

    override fun normal() {
        _normal = true
    }

    override fun reverse() {
        _normal = false
    }
}
