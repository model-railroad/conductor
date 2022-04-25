package com.alfray.conductor.v2.script

data class Delay(val seconds: Int)

val Int.seconds: Delay
    get() = Delay(this)

interface ITimer: IActive {
    val delay: Delay
    fun start()
    fun stop()
    fun reset()
}
