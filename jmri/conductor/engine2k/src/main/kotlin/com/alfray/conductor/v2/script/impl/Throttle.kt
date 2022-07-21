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
import com.alfray.conductor.v2.script.dsl.DccSpeed
import com.alfray.conductor.v2.script.dsl.Delay
import com.alfray.conductor.v2.script.dsl.FBits
import com.alfray.conductor.v2.script.dsl.IThrottle
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject


/**
 * A throttle defined by a script.
 * <p/>
 * The actual JMRI throttle is only assigned via the {@link #onExecStart()} method.
 * <p/>
 * This throttle object keeps track of its state (speed, light/sound state) and only
 * uses its internal state when providing values. JMRI is only used as a setter.
 */
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
    /** Delay in seconds after the last command sent to JMRI before repeating the current speed. */
    internal var repeatSpeedSeconds = Delay(0)
        private set // visible for testing
    private var _f = FBits()
    private var lastJmriTS: Long = 0L
    private var jmriThrottle: IJmriThrottle? = null

    private companion object {
        val TAG: String = Throttle::class.java.simpleName
    }

    /** The last speed set for this engine. */
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
        setSpeed(speed)
    }

    override fun reverse(speed: DccSpeed) {
        setSpeed(speed.reverse())
    }

    override fun stop() {
        setSpeed(DccSpeed(0))
    }

    override fun f(index: Int, on: Boolean) : FBits {
        try {
            jmriThrottle?.triggerFunction(index, on)
        } catch (e: Throwable) {
            logger.d(TAG, "[$dccAddress] triggerFunction exception: $e")
        }
        return f.set(index, on)
    }

    override fun horn() {
        try {
            jmriThrottle?.horn()
        } catch (e: Throwable) {
            logger.d(TAG, "[$dccAddress] horn exception: $e")
        }
        lastJmriTS = clock.elapsedRealtime()
    }

    override fun light(on: Boolean) {
        _light = on
        try {
            jmriThrottle?.setLight(_light)
        } catch (e: Throwable) {
            logger.d(TAG, "[$dccAddress] setLight exception: $e")
        }
        lastJmriTS = clock.elapsedRealtime()
    }

    override fun sound(on: Boolean) {
        _sound = on
        try {
            jmriThrottle?.setSound(_sound)
        } catch (e: Throwable) {
            logger.d(TAG, "[$dccAddress] setSound exception: $e")
        }
        lastJmriTS = clock.elapsedRealtime()
    }

    /** Sets the repeat speed interval. Does nothing if <= 0. */
    override fun repeat(repeat: Delay) {
        repeatSpeedSeconds = repeat
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
     * The call does nothing if [.getRepeatSpeedSeconds] <= 0.
     */
    fun repeatSpeed() {
        if (repeatSpeedSeconds.seconds < 1) {
            return
        }
        val elapsedMs: Long = clock.elapsedRealtime() - lastJmriTS
        if (elapsedMs >= 1000 * repeatSpeedSeconds.seconds) {
            setSpeed(_speed)
        }
    }

    /**
     * Sets the throttle speed and direction.
     * Speed 0 means stopped, a positive number for forward and a negative number for reverse.
     */
    private fun setSpeed(speed: DccSpeed) {
        val speedChange = speed != _speed
        _speed = speed

        try {
            if (speedChange) {
                eventLogger.logAsync(
                    EventLogger.Type.DccThrottle,
                    dccAddress.toString(),
                    speed.speed.toString()
                )
            }
            jmriThrottle?.setSpeed(speed.speed)
        } catch (e: Throwable) {
            logger.d(TAG, "[$dccAddress] setSpeed exception: $e")
        }
        try {
            updateKV(dccAddress, speed.speed)
        } catch (e: Throwable) {
            logger.d(TAG, "[$dccAddress] getDccAddress exception: $e")
        }

        lastJmriTS = clock.elapsedRealtime()
    }

    fun eStop() {
        // Do a "soft" stop to speed 0. This also sets this object's state properly.
        setSpeed(DccSpeed(0))

        // Ask JMRI to send an e-stop command to all throttles
        try {
            jmriThrottle?.eStop()
        } catch (e: Throwable) {
            logger.d(TAG, "[$dccAddress] eStop exception: $e")
        }
    }

    private fun updateKV(address: Int, speed: Int) {
        keyValue.putValue(
            "${Prefix.DccThrottle}$address",
            speed.toString(), true /*broadcast*/
        )
    }
}
