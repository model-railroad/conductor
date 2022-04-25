package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.DccSpeed
import com.alfray.conductor.v2.script.FBits
import com.alfray.conductor.v2.script.IThrottle

class Throttle(override val dccAddress: Int) : IThrottle {
    private var _speed = DccSpeed(0)
    private var _light = false
    private var _sound = false
    private var _f = FBits()

    override val speed: DccSpeed
        get() = _speed
    override val light: Boolean
        get() = _light
    override val sound: Boolean
        get() = _sound
    override val f: FBits
        get() = _f

    override fun forward(speed: DccSpeed) {
        _speed = speed
    }

    override fun reverse(speed: DccSpeed) {
        _speed = speed.reverse()
    }

    override fun stop() {
        _speed = DccSpeed(0)
    }

    override fun horn() {
        println("@@ Horn $dccAddress")
    }

    override fun light(on: Boolean) {
        _light = on
    }

    override fun sound(on: Boolean) {
        _sound = on
    }
}
