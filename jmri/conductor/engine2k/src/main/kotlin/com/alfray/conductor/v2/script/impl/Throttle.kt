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
import com.alfray.conductor.v2.script.CondCache
import com.alfray.conductor.v2.script.CurrentContext
import com.alfray.conductor.v2.script.ExecContext.Reason
import com.alfray.conductor.v2.script.dsl.DccSpeed
import com.alfray.conductor.v2.script.dsl.Delay
import com.alfray.conductor.v2.script.dsl.IFBits
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
    private val condCache: CondCache,
    private val eventLogger: EventLogger,
    private val jmriProvider: IJmriProvider,
    private val currentContext: CurrentContext,
    @Assisted override val dccAddress: Int,
    @Assisted private val builder: ThrottleBuilder?,
) : VarName(), IThrottle, IExecEngine {
    private val TAG = javaClass.simpleName
    private var _speed = DccSpeed(0)
    private var _light = false
    private var _sound = false
    /** Delay in seconds after the last command sent to JMRI before repeating the current speed. */
    internal var repeatSpeedSeconds = Delay(0)
        private set // visible for testing
    private var lastJmriTS: Long = 0L
    private var jmriThrottle: IJmriThrottle? = null
    private val keyName = "${Prefix.DccThrottle}$dccAddress"
    private var _f = FBits(condCache, keyName)

    /** The last speed set for this engine. */
    override val speed: DccSpeed
        get() = condCache.cachedSpeed(_speed, keyName)
    override val light: Boolean
        get() = condCache.cached(_light, keyName, "L")
    override val sound: Boolean
        get() = condCache.cached(_sound, keyName, "S")
    override val f: FBits
        get() = _f

    init {
        builder?.throttle = this
        builder?.name?.let {
            named(builder.name!!)
        }
    }

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

    override fun f(index: Int, on: Boolean) : IFBits {
        enforceContext()
        eventLog("F${index} " + (if (on) "ON" else "OFF"))
        try {
            jmriThrottle?.triggerFunction(index, on)
        } catch (e: Throwable) {
            logger.d(TAG, "[$dccAddress] triggerFunction exception: $e")
        }
        return f.set(index, on)
    }

    override fun horn() {
        enforceContext()
        eventLog("Horn")
        try {
            lastJmriTS = clock.elapsedRealtime()
            jmriThrottle?.horn()
        } catch (e: Throwable) {
            logger.d(TAG, "[$dccAddress] horn exception: $e")
        }
    }

    override fun light(on: Boolean) {
        enforceContext()
        _light = on
        eventLog("Light " + (if (on) "ON" else "OFF"))
        try {
            if (builder?.actionOnLight != null) {
                builder.actionOnLight!!.invoke(on)
            } else {
                lastJmriTS = clock.elapsedRealtime()
                jmriThrottle?.setLight(_light)
            }
        } catch (e: Throwable) {
            logger.d(TAG, "[$dccAddress] setLight exception: $e")
        }
    }

    override fun sound(on: Boolean) {
        enforceContext()
        _sound = on
        eventLog("Sound " + (if (on) "ON" else "OFF"))
        try {
            if (builder?.actionOnSound != null) {
                builder.actionOnSound!!.invoke(on)
            } else {
                lastJmriTS = clock.elapsedRealtime()
                jmriThrottle?.setSound(_sound)
            }
        } catch (e: Throwable) {
            logger.d(TAG, "[$dccAddress] setSound exception: $e")
        }
    }

    override fun bell(on: Boolean) {
        enforceContext()
        eventLog("Bell " + (if (on) "ON" else "OFF"))
        try {
            if (builder?.actionOnBell != null) {
                builder.actionOnBell!!.invoke(on)
            } else {
                lastJmriTS = clock.elapsedRealtime()
                jmriThrottle?.setSound(_sound)
            }
        } catch (e: Throwable) {
            logger.d(TAG, "[$dccAddress] setSound exception: $e")
        }
    }

    /** Sets the repeat speed interval. Does nothing if <= 0. */
    override fun repeat(repeat: Delay) {
        enforceContext()
        repeatSpeedSeconds = repeat
    }

    override fun onExecStart() {
        jmriThrottle = checkNotNull(jmriProvider.getThrottle(dccAddress))
        updateKV()
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
        enforceContext()
        val speedChange = speed != _speed
        _speed = speed

        try {
            if (speedChange) {
                eventLog(speed.speed.toString())
            }
            lastJmriTS = clock.elapsedRealtime()
            jmriThrottle?.setSpeed(speed.speed)
        } catch (e: Throwable) {
            logger.d(TAG, "[$dccAddress] setSpeed exception: $e")
        }
        try {
            updateKV()
        } catch (e: Throwable) {
            logger.d(TAG, "[$dccAddress] getDccAddress exception: $e")
        }

    }

    fun eStop() {
        eventLog("eStop")

        // Do a "soft" stop to speed 0. This also sets this object's state properly.
        setSpeed(DccSpeed(0))

        // Ask JMRI to send an e-stop command to all throttles
        try {
            jmriThrottle?.eStop()
        } catch (e: Throwable) {
            logger.d(TAG, "[$dccAddress] eStop exception: $e")
        }
    }

    private fun updateKV() {
        export(keyName)
    }

    fun export(keyName: String) {
        keyValue.putValue(
            keyName,
            _speed.speed.toString(),
            true /*broadcast*/
        )
    }

    private fun enforceContext() {
        currentContext.assertNotHasReason(TAG, Reason.LOAD_SCRIPT) {
            "ERROR: throttle actions must be called in an event or rule definition."
        }
    }

    private fun eventLog(value: String) {
        eventLogger.logAsync(
            EventLogger.Type.DccThrottle,
            dccAddress.toString(),
            value
        )
    }
}

internal data class FBits(
    private val condCache: CondCache,
    private val keyName: String,
    var f: Int = 0,
) : IFBits {
    override operator fun get(bit: Int) : Boolean =
        condCache.cached((f and (1 shl bit)) != 0, keyName, "F$bit")

    override operator fun set(bit: Int, on: Boolean) : IFBits {
        f = (f and (1 shl bit).inv()) or ((if (on) 1 else 0) shl bit)
        return this
    }
}
