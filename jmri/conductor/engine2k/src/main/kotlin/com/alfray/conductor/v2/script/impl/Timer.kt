package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.ITimer

class Timer(override val seconds: Int) : ITimer {
    val name = "@timer@$seconds"
    var started = false
        private set
    var elapsed : Double = 0.0
        private set

    override val active: Boolean
        get() = elapsed >= seconds

    override fun not(): Boolean = !active

    override fun start() {
        if (!started) {
            started = true
            elapsed = 0.0
        }
    }

    override fun stop() {
        started = false
    }

    override fun reset() {
        started = false
        elapsed = 0.0
    }

    // TODO change to a clock provider with timeElapsed absolute
    fun update(elapsed: Double) {
        // Only increment time if increasing and timer is started.
        // A stopped timer does not update anymore.
        if (started && elapsed >= this.elapsed) {
            this.elapsed = elapsed
        }
    }
}
