package com.alfray.conductor.v2.script

interface IConductor {
    fun sensor(systemName: String)
    fun block(systemName: String)
    fun turnout(systemName: String)
}
