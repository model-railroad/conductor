package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.dsl.IActiveRoute
import com.alfray.conductor.v2.script.dsl.IRoute

internal class RouteIdle(override val owner: IActiveRoute) : IRoute {
    override fun activate() {
        owner.activate(this)
    }
}
