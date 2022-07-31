package com.alfray.conductor.v2.simulator

import com.alflabs.conductor.jmri.IJmriProvider
import com.alflabs.conductor.jmri.IJmriThrottle
import com.alflabs.utils.IClock
import com.alflabs.utils.ILogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.util.Locale

/** Creates a new throttle for the given JMRI DCC Address. */
@AssistedFactory
interface ISimulThrottleFactory {
    fun create(dccAddress: Int) : SimulThrottle
}

class SimulThrottle @AssistedInject constructor(
    private val logger: ILogger,
    private val clock: IClock,
    private val jmriProvider: IJmriProvider,
    @Assisted val dccAddress_: Int
) : IJmriThrottle, IExecSimul {
    private val TAG = javaClass.simpleName
    private var graph: SimulRouteGraph? = null
    internal /*VisibleForTesting*/ var block: SimulRouteBlock? = null
    internal /*VisibleForTesting*/ var graphForward = true
    /** Time in ms we have accumulated on this block. */
    private var blockMS: Long = 0L
    /** Time when the block time was last updated. */
    private var lastTS: Long = 0L
    /** Current engine speed and direction. */
    private var _speed: Int = 0
    /** Max time to spend on this block before moving to the next one. */
    internal val blockMaxMs = 30*1000L /* 5s for debugging TBD make customizable/variable */

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
                if (it.reversal) {
                    graphForward = !graphForward
                }
            }

            block = newBlock
            blockMS = 0
            lastTS = clock.elapsedRealtime()
            logger.d(TAG, String.format("[%04d] Move to block %s then %s",
                dccAddress,
                block,
                if (graphForward) "FWD" else "REV"))
        }
    }

    override fun onExecStart() {
        changeBlock(null)
        graph?.let { g ->
            logger.d(TAG, String.format("[%04d] Route Graph: %s", dccAddress, g.toString()))
            changeBlock(g.start)
            graphForward = true
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
                    graph?.whereTo(b, graphForward)?.let { newBlock -> changeBlock(newBlock) }
                }
            }
        }
    }

    fun getUiLogOutput(): String {
        val sb = StringBuilder(String.format(Locale.US, "[%04d]", dccAddress))

        block?.let { b ->
            sb.append(" on $block")
            if (_speed == 0) {
                sb.append(" STOP")
            } else {
                sb.append(String.format(Locale.US,
                    " for %.1f / %.1f s",
                    blockMS / 1000.0, blockMaxMs / 1000.0))
            }

            graph?.whereTo(b, graphForward)?.let { nb ->
                sb.append(", next to $nb")
            }
            sb.append(if (graphForward) " FWD" else " REV")
        }

        return sb.toString()
    }
}
