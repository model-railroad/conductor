package com.alfray.conductor.v2.script.dsl

interface ISensor : IActive {
    fun active(isActive: Boolean)
    val systemName: String
}
