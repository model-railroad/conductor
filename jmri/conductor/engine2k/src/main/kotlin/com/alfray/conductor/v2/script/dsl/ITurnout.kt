package com.alfray.conductor.v2.script.dsl

interface ITurnout : IActive {
    val systemName: String
    val normal: Boolean
    fun normal()
    fun reverse()
}
