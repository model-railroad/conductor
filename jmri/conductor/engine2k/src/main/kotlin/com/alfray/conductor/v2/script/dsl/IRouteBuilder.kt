package com.alfray.conductor.v2.script.dsl

interface IRouteBuilder {
    fun idle(): IRoute
    fun sequence(init: IRouteSequenceBuilder.() -> Unit): IRoute
}
