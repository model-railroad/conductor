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

package com.alfray.conductor.v2.script.impl

import com.alflabs.conductor.jmri.IJmriProvider
import com.alflabs.conductor.jmri.IJmriThrottle
import com.alflabs.conductor.util.EventLogger
import com.alflabs.kv.IKeyValue
import com.alflabs.manifest.Prefix
import com.alflabs.utils.IClock
import com.alflabs.utils.ILogger
import com.alfray.conductor.v2.dagger.Script2kScope
import com.alfray.conductor.v2.script.dsl.DccSpeed
import com.alfray.conductor.v2.script.dsl.Delay
import com.alfray.conductor.v2.script.dsl.FBits
import com.alfray.conductor.v2.script.dsl.IThrottle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

@Script2kScope
@AssistedFactory
internal interface IThrottleFactory {
    fun create(dccAddress: Int) : Throttle
}

internal class Throttle @AssistedInject constructor(
    private val clock: IClock,
    private val logger: ILogger,
    private val keyValue: IKeyValue,
    private val eventLogger: EventLogger,
    private val jmriProvider: IJmriProvider,
    @Assisted override val dccAddress: Int
) : VarName(), IThrottle, IExecEngine {
    private var _speed = DccSpeed(0)
    private var _light = false
    private var _sound = false
    private var _repeat = Delay(0)
    private var _f = FBits()
    /** Delay in seconds after the last command sent to JMRI before repeating the current speed. */
    private var repeatSpeedSeconds = 0
    private var lastJmriTS: Long = 0L
    private lateinit var jmriThrottle: IJmriThrottle

    private companion object {
        val TAG: String = Throttle::class.java.simpleName
    }

    override val speed: DccSpeed
        get() = _speed
    override val light: Boolean
        get() = _light
    override val sound: Boolean
        get() = _sound
    override val f: FBits
        get() = _f

    override fun named(name: String): IThrottle {
        setNamed(name)
        return this
    }

    override fun defaultName(): String = "Throttle-$dccAddress"

    override fun forward(speed: DccSpeed) {
        _speed = speed
    }

    override fun reverse(speed: DccSpeed) {
        _speed = speed.reverse()
    }

    override fun stop() {
        _speed = DccSpeed(0)
    }

    override fun horn() {
        // println("@@ Horn $dccAddress")
    }

    override fun light(on: Boolean) {
        _light = on
    }

    override fun sound(on: Boolean) {
        _sound = on
    }

    override fun repeat(repeat: Delay) {
        _repeat = repeat
    }

    override fun onExecStart() {
        jmriThrottle = checkNotNull(jmriProvider.getThrottle(dccAddress))
        updateKV(dccAddress, _speed.speed)
    }

    override fun onExecHandle() {
        // no-op
    }

    /**
     * Repeats the current speed if the specified delay as expired between now and the
     * last command sent to JMRI for this throttle.
     *
     *
     * The call does nothing if [.getRepeatSpeedSeconds] <= 0.
     */
    fun repeatSpeed() {
        if (repeatSpeedSeconds < 1) {
            return
        }
        val elapsedMs: Long = clock.elapsedRealtime() - lastJmriTS
        if (elapsedMs >= 1000 * repeatSpeedSeconds) {
            setSpeed(_speed)
        }
    }

    /**
     * Sets the throttle speed and direction.
     * Speed 0 means stopped, a positive number for forward and a negative number for reverse.
     */
    fun setSpeed(speed: DccSpeed) {
        val speedChange = speed != _speed
        _speed = speed

        val dccAddress = jmriThrottle.dccAddress
        try {
            if (speedChange) {
                eventLogger.logAsync(
                    EventLogger.Type.DccThrottle,
                    dccAddress.toString(),
                    speed.speed.toString()
                )
            }
            jmriThrottle.setSpeed(speed.speed)
        } catch (e: Throwable) {
            logger.d(
                TAG,
                "[$dccAddress] setSpeed exception: $e"
            )
        }
        try {
            updateKV(dccAddress, speed.speed)
        } catch (e: Throwable) {
            logger.d(
                TAG,
                "[$dccAddress] getDccAddress exception: $e"
            )
        }

        lastJmriTS = clock.elapsedRealtime()
// TODO(implement throttle speed listener)
//        if (speedListener != null && speedChange) {
//            try {
//                speedListener.accept(speed.speed)
//            } catch (e: Throwable) {
//                logger.d(
//                    TAG,
//                    "[$dccAddress] mSpeedListener exception: $e"
//                )
//            }
//        }
    }

// TODO(implement throttle speed listener)
//    fun setSpeedListener(speedListener: IIntFunction) {
//        this.speedListener = speedListener
//    }

    fun eStop() {
        // Do a "soft" stop to speed 0. This also sets this object's state properly.
        setSpeed(DccSpeed(0))

        // Ask JMRI to send an e-stop command to all throttles
        try {
            jmriThrottle.eStop()
        } catch (e: Throwable) {
            logger.d(
                TAG,
                "[$dccAddress] eStop exception: $e"
            )
        }
    }

    private fun updateKV(address: Int, speed: Int) {
        keyValue.putValue(
            "${Prefix.DccThrottle}$address",
            speed.toString(), true /*broadcast*/
        )
    }
}
