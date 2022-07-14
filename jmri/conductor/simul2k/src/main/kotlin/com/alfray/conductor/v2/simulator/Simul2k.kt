package com.alfray.conductor.v2.simulator

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Simul2k @Inject constructor(
    val jmriProvider: SimulJmriProvider
) : IExecSimul {
    /** The script is about to be loaded or reloaded. Clear any internal references. */
    fun onReload() {
        jmriProvider.clear()
    }

    /** Add a route definition after the script has been loaded. */
    fun addRoute(dccAddress: Int, graph: SimulRouteGraph) {
        val t = jmriProvider.getThrottle(dccAddress) as SimulThrottle
        t.mergeGraph(graph)
    }

    /** Called once before the main exec/simulation loop. */
    override fun onExecStart() {
        jmriProvider.onExecStart()
    }

    /** Repeated called during the main exec/simulation loop. */
    override fun onExecHandle() {
        jmriProvider.onExecHandle()
    }
}
