package com.alfray.conductor.v2.simulator

import com.alflabs.conductor.jmri.IJmriProvider
import com.alflabs.conductor.jmri.IJmriThrottle
import com.alflabs.utils.IClock
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
    private val clock: IClock,
    private val jmriProvider: IJmriProvider,
    @Assisted val dccAddress_: Int
) : IJmriThrottle, IExecSimul {
    private val TAG = javaClass.simpleName
    private var graph: SimulRouteGraph? = null
    private var block: SimulRouteBlock? = null
    /** Time in ms we have accumulated on this block. */
    private var blockMS: Long = 0
    /** Time when the block time was last updated. */
    private var lastTS: Long = 0
    /** Current engine speed and direction. */
    private var _speed: Int = 0
    /** Max time to spend on this block before moving to the next one. */
    private val blockMaxMs = 5*1000 /* 5s for debugging TBD make customizable/variable */

    override fun eStop() {
        _speed = 0
        logger.d(TAG, String.format("[%04d] E-Stop", dccAddress))
    }

    override fun setSpeed(speed: Int) {
        _speed = speed
        logger.d(TAG, String.format("[%04d] Speed: %d", dccAddress, speed))
    }

    override fun setSound(on: Boolean) {
        logger.d(TAG, String.format("[%04d] Sound: %s", dccAddress, on))
    }

    override fun setLight(on: Boolean) {
        logger.d(TAG, String.format("[%04d] Light: %s", dccAddress, on))
    }

    override fun horn() {
        logger.d(TAG, String.format("[%04d] Horn", dccAddress))
    }

    override fun triggerFunction(function: Int, on: Boolean) {
        logger.d(TAG, String.format("[%04d] F%d: %s", dccAddress, function, on))
    }

    override fun getDccAddress(): Int {
        return dccAddress_
    }

    fun mergeGraph(newGraph: SimulRouteGraph) {
        graph = graph?.merge(newGraph) ?: newGraph
    }

    private fun changeBlock(newBlock: SimulRouteBlock?) {
        val oldBlock = block
        if (newBlock != oldBlock) {
            oldBlock?.let {
                jmriProvider.getSensor(it.systemName)?.isActive = false
            }
            newBlock?.let {
                jmriProvider.getSensor(it.systemName)?.isActive = true
            }

            block = newBlock
            blockMS = 0
            lastTS = clock.elapsedRealtime()
            logger.d(TAG, String.format("[%04d] Move To Block %s", dccAddress, block))
        }
    }

    override fun onExecStart() {
        changeBlock(null)
        graph?.let { g ->
            logger.d(TAG, String.format("[%04d] Route Graph: %s", dccAddress, g.toString()))
            changeBlock(g.start)
        }
    }

    override fun onExecHandle() {
        block?.let { b ->
            val nowTS = clock.elapsedRealtime()
            if (_speed == 0) {
                lastTS = nowTS
                blockMS = 0
            } else {
                blockMS += nowTS - lastTS
                lastTS = nowTS

                if (blockMS >= blockMaxMs) {
                    // change blocks
                    graph?.whereTo(b)?.let { newBlock -> changeBlock(newBlock) }
                }
            }
        }
    }
}
