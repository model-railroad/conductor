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
import com.alflabs.utils.IClock
import com.alfray.conductor.v2.script.CondCache
import com.alfray.conductor.v2.simulator.SimulRouteBlock
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * A block matching an underlying JMRI sensor, as defined by a script.
 * <p/>
 * The actual JMRI sensor is only assigned via the [onExecStart] method.
 * <p/>
 * The system name should be unique across all blocks (both real and virtual blocks) as it is used
 * to uniquely identify a block.
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
    private val jmriProvider: IJmriProvider,
    clock: IClock,
    condCache: CondCache,
    eventLogger: EventLogger,
    @Assisted systemName: String
) : BlockBase(clock, condCache, eventLogger, systemName) {
    private var jmriSensor: IJmriSensor? = null
    private var _active = false

    /**
     * Internally set the active block state. Useful for testing purposes. Not exposed to DSL.
     * Note that the [active] method is a no-op for [Block] whereas this actually forces the
     * underlying sensor's active state.
     * The internal state changed here will only be taken into account after the next call to
     * [onExecStart] or [onExecHandle].
     */
    internal fun internalActive(isActive: Boolean) {
        // Updates the internal state only.
        _active = isActive
        jmriSensor?.let { it.isActive = isActive }
    }

    /**
     * Called by the DSL to mark a block as occupied/active.
     * This is a no-op for JMRI-backed blocks.
     */
    override fun active(isActive: Boolean) {
        // no-op
    }

    /** Initializes the underlying JMRI sensor. */
    override fun onExecStart() {
        jmriSensor = checkNotNull(jmriProvider.getSensor(systemName))
        // Start with an invalid state, to force an update in the first onExecHandle call.
        jmriSensor?.let { lastActive = !it.isActive }
        // Now check with JMRI and send the first eventLogger + keyValue events.
        onExecHandle()
    }

    override fun onExecHandle() {
        // Update the state from JMRI
        jmriSensor?.let { _active = it.isActive }
        if (_active != lastActive) {
            lastActive = _active
            val value = if (lastActive) Constants.On else Constants.Off
            eventLogger.logAsync(
                    EventLogger.Type.Sensor,
                    "$keyName $name",
                    value)
            keyValue.putValue(keyName, value, true /*broadcast*/)
        }
    }

    override fun toSimulRouteBlock(reversal: Boolean?): SimulRouteBlock =
        SimulRouteBlock(systemName, name, virtual = false, reversal ?: false)
}
