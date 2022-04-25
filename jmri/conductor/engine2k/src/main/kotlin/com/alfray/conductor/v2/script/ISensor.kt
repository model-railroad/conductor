package com.alfray.conductor.v2.script

interface ISensor : IActive {
    fun active(isActive: Boolean)
    val systemName: String
}
