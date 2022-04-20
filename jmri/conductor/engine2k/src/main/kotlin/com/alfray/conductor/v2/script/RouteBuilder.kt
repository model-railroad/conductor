package com.alfray.conductor.v2.script

import com.alfray.conductor.v2.script.impl.RouteIdle

class RouteBuilder {
    fun idle(): IRoute {
        return RouteIdle()
    }

    fun sequence(init: IRouteSequenceBuilder.() -> Unit): IRoute {
        val builder = RouteSequenceBuilder()
        builder.init()
        return builder.create()
    }
}
