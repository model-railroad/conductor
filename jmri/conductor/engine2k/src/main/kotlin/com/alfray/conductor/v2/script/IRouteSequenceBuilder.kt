package com.alfray.conductor.v2.script

import com.alfray.conductor.v2.script.impl.RouteSequence

interface IRouteSequenceBuilder {

}

class RouteSequenceBuilder : IRouteSequenceBuilder {
    fun create() : IRoute = RouteSequence(this)
}
