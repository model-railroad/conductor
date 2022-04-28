package com.alfray.conductor.v2.script.dsl

interface IRoute {
    val owner: IActiveRoute
    fun activate()
}
