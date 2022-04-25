package com.alfray.conductor.v2.script

//data class DccSpeed(val speed: Int)
//fun Int.speed() : DccSpeed = DccSpeed(this)

interface IThrottle {
    val dccAddress: Int
    val speed: Int
    val light: Boolean
    val sound: Boolean
    val stopped: Boolean
    val f1: Boolean
    val f5: Boolean
    val f9: Boolean

    fun forward(speed: Int)
    fun reverse(speed: Int)
    fun stop()
    fun horn()
    fun light(on: Boolean)
    fun sound(on: Boolean)
    fun f1(on: Boolean)
    fun f5(on: Boolean)
    fun f9(on: Boolean)
}
