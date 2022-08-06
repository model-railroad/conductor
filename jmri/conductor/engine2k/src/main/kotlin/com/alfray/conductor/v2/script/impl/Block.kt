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
import com.alflabs.conductor.jmri.IJmriSensor
import com.alflabs.conductor.util.EventLogger
import com.alflabs.kv.IKeyValue
import com.alflabs.manifest.Constants
import com.alflabs.manifest.Prefix
import com.alfray.conductor.v2.script.CondCache
import com.alfray.conductor.v2.script.dsl.IBlock
import com.alfray.conductor.v2.simulator.SimulRouteBlock
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * A block matching an underlying JMRI sensor, as defined by a script.
 * <p/>
 * The actual JMRI sensor is only assigned via the [onExecStart] method.
 * <p/>
 * Querying the active state returns the internal state and does NOT read from JMRI;
 * reading from JMRI is only done in [onExecHandle] to guarantee to be in sync with the exec loop.
 * <p/>
 * The main difference between a Block and a Sensor resides in the concept of an active block:
 * a block is active when its underlying physical sensor is active OR when a route manager
 * determines that a train must logically/virtually be occupying that block.
 * A block has 3 states: empty, occupied (sensor active), and trailing (sensor either active or not).
 * The state is computed by an external route manager which updates the block and any nodes that
 * depend on this block.
 *
 * Block equality purely relies on the equality of the unique systemName ID.
 * The active state of the block is NOT used in equality. This ensures stability
 * when used as a map key.
 */
internal class Block @AssistedInject constructor(
    private val keyValue: IKeyValue,
    private val condCache: CondCache,
    private val eventLogger: EventLogger,
    private val jmriProvider: IJmriProvider,
    @Assisted override val systemName: String
) : VarName(), IBlock, IExecEngine {
    private var jmriSensor: IJmriSensor? = null
    private var _active = false
    private var lastActive = false
    private val keyName = "${Prefix.Block}$systemName"
    override val active: Boolean
        get() = condCache.cached(_active, keyName) // uses internal state, does NOT update from JMRI.
    /** Occupancy state of the block: empty, occupied, or trailing. */
    var state = State.EMPTY
        private set

    override fun not(): Boolean = !active

    /** Internally set the active block state. Useful for testing purposes. */
    internal fun active(isActive: Boolean) {
        // Updates the internal state only.
        _active = isActive
    }

    override fun named(name: String): IBlock {
        setNamed(name)
        return this
    }

    override fun defaultName(): String = systemName

    enum class State {
        EMPTY,
        OCCUPIED,
        TRAILING
    }

    fun changeState(newState: State) {
        state = newState
    }

    /** Initializes the underlying JMRI sensor. */
    override fun onExecStart() {
        jmriSensor = checkNotNull(jmriProvider.getSensor(systemName))
        onExecHandle()
    }

    override fun onExecHandle() {
        // Update the state from JMRI
        jmriSensor?.let { _active = it.isActive }
        val value = if (_active) Constants.On else Constants.Off
        keyValue.putValue(keyName, value, true /*broadcast*/)
        if (_active != lastActive) {
            lastActive = _active
            eventLogger.logAsync(EventLogger.Type.Sensor, keyName, value)
        }
    }

    /**
     * Block equality purely relies on the equality of the unique systemName ID.
     * The active state of the block is NOT used in equality. This ensures stability
     * when used as a map key.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Block

        if (systemName != other.systemName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = systemName.hashCode()
        return result
    }

    override fun toString(): String {
        var s = name
        if (s != systemName) {
            s = "$s [$systemName]"
        }
        return if (_active) "<$s>" else "{$s}"
    }

    fun toSimulRouteBlock(reversal: Boolean?): SimulRouteBlock =
        SimulRouteBlock(systemName, name, reversal ?: false)
}
