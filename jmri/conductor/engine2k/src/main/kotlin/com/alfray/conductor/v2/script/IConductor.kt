package com.alfray.conductor.v2.script

interface IConductor {
    fun sensor(systemName: String): ISensor
    fun block(systemName: String): IBlock
    fun turnout(systemName: String): ITurnout
    fun timer(seconds: Int): ITimer
    fun throttle(dccAddress: Int): IThrottle
}
