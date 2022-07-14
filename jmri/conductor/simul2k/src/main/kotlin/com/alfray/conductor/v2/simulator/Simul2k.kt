package com.alfray.conductor.v2.simulator

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Simul2k @Inject constructor(
    val jmriProvider: SimulJmriProvider
) {
    fun clear() {
        jmriProvider.clear()
    }

    fun addRoute(dccAddress: Int, graph: SimulRouteGraph) {
        val t = jmriProvider.getThrottle(dccAddress) as SimulThrottle
        t.mergeGraph(graph)
    }
}
