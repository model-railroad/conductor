package com.alfray.conductor.v2.script

interface IActive {
    val active: Boolean
    operator fun not() : Boolean
}
