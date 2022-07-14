package com.alfray.conductor.v2.simulator

import com.alflabs.conductor.jmri.IJmriThrottle
import com.alflabs.utils.ILogger

class SimulThrottle constructor(
    private val dccAddress_: Int,
    private val logger: ILogger
) : IJmriThrottle {
    private val TAG = javaClass.simpleName
    var graph: SimulRouteGraph? = null
        private set

    override fun eStop() {
        logger.d(TAG, String.format("[%d] E-Stop", dccAddress))
    }

    override fun setSpeed(speed: Int) {
        logger.d(TAG, String.format("[%d] Speed: %d", dccAddress, speed))
    }

    override fun setSound(on: Boolean) {
        logger.d(TAG, String.format("[%d] Sound: %s", dccAddress, on))
    }

    override fun setLight(on: Boolean) {
        logger.d(TAG, String.format("[%d] Light: %s", dccAddress, on))
    }

    override fun horn() {
        logger.d(TAG, String.format("[%d] Horn", dccAddress))
    }

    override fun triggerFunction(function: Int, on: Boolean) {
        logger.d(TAG, String.format("[%d] F%d: %s", dccAddress, function, on))
    }

    override fun getDccAddress(): Int {
        return dccAddress_
    }

    fun mergeGraph(newGraph: SimulRouteGraph) {
        graph = graph?.merge(newGraph) ?: newGraph
    }
}
