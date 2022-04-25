package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.IThrottle

class Throttle(override val dccAddress: Int) : IThrottle {
    private var speedInternal = 0
    private var lightInternal = false

    override val speed: Int
        get() = speedInternal
    override val light: Boolean
        get() = lightInternal
    override val sound: Boolean
        get() = TODO("Not yet implemented")
    override val stopped: Boolean
        get() = TODO("Not yet implemented")
    override val f1: Boolean
        get() = TODO("Not yet implemented")
    override val f5: Boolean
        get() = TODO("Not yet implemented")
    override val f9: Boolean
        get() = TODO("Not yet implemented")
    

    override fun forward(speed: Int) {
        speedInternal = speed
    }

    override fun reverse(speed: Int) {
        speedInternal = -speed
    }

    override fun stop() {
        speedInternal = 0
    }

    override fun horn() {
        TODO("Not yet implemented")
    }

    override fun light(on: Boolean) {
        lightInternal = on
    }

    override fun sound(on: Boolean) {
        TODO("Not yet implemented")
    }

    override fun f1(on: Boolean) {
        TODO("Not yet implemented")
    }

    override fun f5(on: Boolean) {
        TODO("Not yet implemented")
    }

    override fun f9(on: Boolean) {
        TODO("Not yet implemented")
    }
}
