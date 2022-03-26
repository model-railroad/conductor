package com.alfray.conductor.v2.script

interface ITimer {
    val seconds: Int
}

class Timer(override val seconds: Int) : ITimer {
}
