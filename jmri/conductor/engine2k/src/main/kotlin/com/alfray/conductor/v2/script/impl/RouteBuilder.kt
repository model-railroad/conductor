package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.dsl.IRoute
import com.alfray.conductor.v2.script.dsl.IRouteBuilder
import com.alfray.conductor.v2.script.dsl.IRouteSequenceBuilder
import com.alfray.conductor.v2.script.dsl.RouteSequenceBuilder

internal class RouteBuilder : IRouteBuilder {
    override fun idle(): IRoute {
        return RouteIdle()
    }

    override fun sequence(init: IRouteSequenceBuilder.() -> Unit): IRoute {
        val builder = RouteSequenceBuilder()
        builder.init()
        return builder.create()
    }
}
