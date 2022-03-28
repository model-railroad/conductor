package com.alfray.conductor.v2.script

interface ITimer: IActive {
    val seconds: Int
    fun start()
    fun stop()
}
