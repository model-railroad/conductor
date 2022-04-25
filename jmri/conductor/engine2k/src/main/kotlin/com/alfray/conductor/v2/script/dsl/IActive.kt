package com.alfray.conductor.v2.script.dsl

interface IActive {
    val active: Boolean
    operator fun not() : Boolean
}
