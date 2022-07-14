package com.alfray.conductor.v2.simulator

import com.alflabs.conductor.jmri.IJmriThrottle
import com.alflabs.utils.ILogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

/** Creates a new throttle for the given JMRI DCC Address. */
@AssistedFactory
interface ISimulThrottleFactory {
    fun create(sdccAddress_: Int) : SimulThrottle
}

class SimulThrottle @AssistedInject constructor(
    private val logger: ILogger,
    @Assisted val dccAddress_: Int
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
