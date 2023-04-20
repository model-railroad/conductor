/*
 * Project: Conductor
 * Copyright (C) 2022 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alfray.conductor.v2.simulator

import com.alflabs.conductor.jmri.IJmriProvider
import com.alflabs.conductor.jmri.IJmriThrottle
import com.alflabs.utils.IClock
import com.alflabs.utils.ILogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/**
 * Simulated throttle.
 *
 * This "drives" the simulator.
 * Once a route has an active throttle and a graph, the simulated throttle updates the blocks'
 * states. A rough timing is computed for each block to determine how long to wait before moving
 * to the next predicted block.
 *
 */
class SimulThrottle @AssistedInject constructor(
    private val logger: ILogger,
    private val clock: IClock,
    private val jmriProvider: IJmriProvider,
    private val simulScheduler: SimulScheduler,
    @Assisted val dccAddress_: Int
) : IJmriThrottle, IExecSimul {
    private val TAG = javaClass.simpleName
    /** The route timeout, in seconds. 0 to deactivate. */
    var routeTimeout: Int = 0
    /** The expected block graph for that throttle. */
    private var graph: SimulRouteGraph? = null
    /** The block simulating the current engine location for that throttle. */
    internal /*VisibleForTesting*/ var block: SimulRouteBlock? = null
    /** Direction the engine is moving in the route graph. */
    internal /*VisibleForTesting*/ var graphForward = true
    /** Time in ms we have accumulated on this block. */
    private var blockMS: Long = 0L
    /** Time when the block time was last updated. */
    private var lastTS: Long = 0L
    /** Current engine speed and direction. */
    private var _speed: Int = 0
    /**
     * Max time to spend on this block before moving to the next one.
     * This is a dynamic property that uses a base 6-second time + any extra timer duration
     * from the current block.
     * If the route has a defined timeout, the block time cannot be longer than the given timeout.
     */
    internal val blockMaxMS: Int
        get() {
            var sec = 6
            block?.let { sec += it.extraTimersSec }
            if (routeTimeout > 1) sec = min(sec, routeTimeout - 1)
            return sec * 1000
        }

    override fun eStop() {
        _speed = 0
        logger.d(TAG, String.format("[%04d] E-Stop", dccAddress))
    }

    override fun setSpeed(speed: Int) {
        if (speed != _speed) {
            logger.d(TAG, String.format("[%04d] Speed: %d", dccAddress, speed))
        }
        _speed = speed
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

    fun setGraph(newGraph: SimulRouteGraph) {
        graph = newGraph

        var newBlock = block
        // If we have a current block reference, validate that block is on that route.
        if (newBlock != null && newBlock !in newGraph.blocks) {
            newBlock = null
        }
        // If the current block is not active, we'll need a different one.
        newBlock?.let {
            if (jmriProvider.getSensor(it.systemName)?.isActive != true) {
                newBlock = null
            }
        }

        // Can we select an occupied block on that route?
        if (newBlock == null) {
            for (b in newGraph.blocks) {
                val sensor = jmriProvider.getSensor(b.systemName)
                if (sensor?.isActive == true) {
                    newBlock = b
                    break
                }
            }
        }

        // As a last resort, take the start block of the graph
        if (newBlock == null) {
            newBlock = newGraph.start
        }

        logger.d(TAG, "[Throttle $dccAddress_] Selected block $newBlock")
        changeBlock(newBlock)
        graphForward = true
    }

    private fun changeBlock(newBlock: SimulRouteBlock?) {
        val oldBlock = block
        if (newBlock != oldBlock) {
            logger.d(TAG, String.format("[%04d] Move to block %s then %s",
                dccAddress,
                newBlock,
                if (graphForward) "FWD" else "REV"))

            newBlock?.let { b ->
                if (!b.virtual) {
                    logger.d(TAG, "[Throttle $dccAddress_] ACTIV block $b to true")
                    val sensor = jmriProvider.getSensor(b.systemName)!!
                    simulScheduler.forceExec(b)
                    sensor.isActive = true
                }
                if (b.reversal) {
                    graphForward = !graphForward
                }
            }

            oldBlock?.let { b ->
                if (!b.virtual) {
                    val sensor = jmriProvider.getSensor(b.systemName)!!
                    val millis = delayTransitionMS(this._speed)
                    simulScheduler.scheduleAfter(millis, b) {
                        logger.d(
                            TAG,
                            "[Throttle $dccAddress_] RESET block $b to false after $millis ms")
                        sensor.isActive = false
                    }
                }
            }

            block = newBlock
            blockMS = 0
            lastTS = clock.elapsedRealtime()

            logger.d(TAG, String.format("[%04d] Simulate time on block %s = %d ms",
                dccAddress,
                newBlock,
                blockMaxMS))
        }
    }

    private fun delayTransitionMS(speed: Int): Int {
        // Speed  1 --> 3   seconds
        // Speed 10 --> 0.5 seconds
        val maxSec = 3.0
        val minSec = 0.5
        val secs: Double = maxSec - (maxSec - minSec) * max(0.0, speed - minSec) / (10.0 - 1.0)
        return (1000.0 * min(maxSec, max(minSec, secs))).toInt()
    }

    override fun onExecStart() {
        graph?.let { g ->
            logger.d(TAG, String.format("[%04d] Route Graph: %s", dccAddress, g.toString()))
        }
    }

    override fun onExecHandle() {
        block?.let { b ->
            val nowTS = clock.elapsedRealtime()
            val sensor : SimulSensor? =
                if (b.virtual) null
                else (jmriProvider.getSensor(b.systemName)!! as SimulSensor)
            sensor?.setRandomize(0.0)

            if (_speed == 0) {
                lastTS = nowTS
                blockMS = 0
            } else {
                blockMS += nowTS - lastTS
                lastTS = nowTS

                if (blockMS >= blockMaxMS) {
                    // change blocks
                    graph?.whereTo(b, graphForward)?.let { newBlock -> changeBlock(newBlock) }
                } else {
                    // Simulate a flaky "blinking" active block only when the engine is moving.
                    jmriProvider as ISimulUiCallback
                    sensor?.setRandomize(if (jmriProvider.isFlaky) 0.01 else 0.0)
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
                    blockMS / 1000.0, blockMaxMS / 1000.0))
            }

            graph?.whereTo(b, graphForward)?.let { nb ->
                sb.append(", next to $nb")
            }
            sb.append(if (graphForward) " FWD" else " REV")
        }

        return sb.toString()
    }
}
