package com.alfray.conductor.v2.script.dsl

data class Delay(val seconds: Int)

val Int.seconds: Delay
    get() = Delay(this)

interface ITimer: IActive {
    val name: String
    val delay: Delay
    fun start()
    fun stop()
    fun reset()
}
