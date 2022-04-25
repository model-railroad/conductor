package com.alfray.conductor.v2.script

//data class Delay(val seconds: Int)
//fun Int.seconds() : Delay = Delay(this)

interface ITimer: IActive {
    val seconds: Int
    fun start()
    fun stop()
    fun reset()
}
