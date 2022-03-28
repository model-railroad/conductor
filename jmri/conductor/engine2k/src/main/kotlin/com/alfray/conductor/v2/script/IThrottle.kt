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
