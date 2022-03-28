package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.ITimer

class Timer(override val seconds: Int) : ITimer {
    private var started = false

    override val active: Boolean
        get() = false

    override fun start() {
        started = true
    }

    override fun stop() {
        started = false
    }
}
