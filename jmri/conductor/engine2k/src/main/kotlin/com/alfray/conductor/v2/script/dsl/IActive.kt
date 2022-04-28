package com.alfray.conductor.v2.script.dsl

interface IActive {
    /** Object is active. */
    val active: Boolean
    /** Object is not active. */
    operator fun not() : Boolean
}
