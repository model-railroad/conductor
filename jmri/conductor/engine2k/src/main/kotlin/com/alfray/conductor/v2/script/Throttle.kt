package com.alfray.conductor.v2.script

interface IThrottle {
    val dccAddress: Int
}

class Throttle(override val dccAddress: Int) : IThrottle {
}
