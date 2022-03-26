package com.alfray.conductor.v2.script

interface ITimer: IActive {
    val seconds: Int
    fun start()
    fun stop()
}

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
