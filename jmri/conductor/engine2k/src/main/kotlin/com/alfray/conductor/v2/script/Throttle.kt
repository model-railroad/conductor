package com.alfray.conductor.v2.script

interface IThrottle {
    val dccAddress: Int
    val speed: Int
    val light: Boolean

    fun horn()
    fun light(on: Boolean)
    fun forward(speed: Int)
    fun reverse(speed: Int)
    fun stop()
}

class Throttle(override val dccAddress: Int) : IThrottle {
    private var speedInternal = 0
    private var lightInternal = false

    override val light: Boolean
        get() = lightInternal

    override val speed: Int
        get() = speedInternal

    override fun horn() {
        TODO("Not yet implemented")
    }

    override fun light(on: Boolean) {
        lightInternal = on
    }

    override fun forward(speed: Int) {
        speedInternal = speed
    }

    override fun reverse(speed: Int) {
        speedInternal = -speed
    }

    override fun stop() {
        speedInternal = 0
    }
}
