package com.alfray.conductor.v2.simulator

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Simul2k @Inject constructor() {

    val throttles = mutableMapOf<Int, SimulThrottle>()

    fun clear() {
        throttles.clear()
    }

    fun addRoute(dccAddress: Int, graph: SimulRouteGraph) {
        throttles.merge(dccAddress, SimulThrottle(dccAddress, graph), SimulThrottle::mergeGraph)
    }

}

class SimulThrottle(val dccAddress: Int, var graph: SimulRouteGraph) {

    fun mergeGraph(newThrottle: SimulThrottle): SimulThrottle {
        return SimulThrottle(dccAddress, graph.merge(newThrottle.graph))
    }

}
