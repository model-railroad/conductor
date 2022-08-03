package com.alfray.conductor.v2.simulator

import com.alflabs.utils.ILogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Simul2k @Inject constructor(
    val jmriProvider: SimulJmriProvider
) : IExecSimul, ISimulCallback {
    private val TAG = javaClass.simpleName
    private val logger: ILogger = jmriProvider

    /** The script is about to be loaded or reloaded. Clear any internal references. */
    fun onReload() {
        jmriProvider.clear()
    }

    /** Add a route definition after the script has been loaded. */
    fun addRoute(dccAddress: Int, graph: SimulRouteGraph) {
        logger.d(TAG, "Add simulated route: $graph")
        val t = jmriProvider.getThrottle(dccAddress) as SimulThrottle
        t.mergeGraph(graph)
    }

    /** Called once before the main exec/simulation loop. */
    override fun onExecStart() {
        logger.d(TAG, "onExecStart")
        jmriProvider.onExecStart()
    }

    /** Repeated called during the main exec/simulation loop. */
    override fun onExecHandle() {
        jmriProvider.onExecHandle()
    }

    /** Notifies the simulator that the sum of timers for that block have changed. */
    override fun onBlockTimersChanged(systemName: String, sumTimersSec: Int) {
        jmriProvider.onBlockTimersChanged(systemName, sumTimersSec)
    }

    fun getUiLogOutput(): String = jmriProvider.getUiLogOutput()
}
