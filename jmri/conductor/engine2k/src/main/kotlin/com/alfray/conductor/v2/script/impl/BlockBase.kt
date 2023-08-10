/*
 * Project: Conductor
 * Copyright (C) 2023 alf.labs gmail com,
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

import com.alflabs.conductor.util.EventLogger
import com.alflabs.manifest.Prefix
import com.alflabs.utils.IClock
import com.alfray.conductor.v2.script.CondCache
import com.alfray.conductor.v2.script.dsl.IBlock
import com.alfray.conductor.v2.simulator.SimulRouteBlock

/**
 * A block matching an underlying JMRI sensor, or a virtual block, as defined by a script.
 * <p/>
 * Implementation common to [Block] and [VirtualBlock].
 */
internal abstract class BlockBase(
    private val clock: IClock,
    private val condCache: CondCache,
    protected val eventLogger: EventLogger,
    final override val systemName: String
) : VarName(), IBlock, INodeBlock, IExecEngine {
    protected var lastActive = false
    protected val keyName = "${Prefix.Block}$systemName"
    /** The cached active property of the underlying sensor. Updated in [onExecHandle] only. */
    override val active: Boolean
        get() = condCache.cached(lastActive, keyName) // uses internal state, does NOT update from JMRI.
    /** Occupancy state of the block: empty, occupied, or trailing. */
    override var state = IBlock.State.EMPTY
        protected set
    /** System millis timestamp of the last state change. 0L when not initialized */
    private var stateTS: Long = 0L

    override fun not(): Boolean = !active

    /**
     * Called by the DSL to mark a block as occupied/active.
     * This is a no-op for JMRI-backed blocks, and real for virtual blocks.
     */
    abstract override fun active(isActive: Boolean)

    override fun named(name: String): IBlock {
        setNamed(name)
        return this
    }

    override fun defaultName(): String = systemName

    /** Internal method used by a Node to change the Block occupancy state. */
    override fun changeState(newState: IBlock.State) {
        val nowMs = clock.elapsedRealtime()
        if (state != newState) {
            val deltaMs = if (stateTS == 0L) -1 else nowMs - stateTS
            eventLogger.logAsync(
                    EventLogger.Type.Block,
                    "$keyName $name",
                    if (deltaMs < 0) newState.name else String.format("%s after %.2f seconds",
                        newState.name, deltaMs.toDouble() / 1000.0))
        }
        state = newState
        stateTS = nowMs
    }

    /**
     * Returns time spent in the block in the current state.
     *
     * Time is the delta since the last state change, if any.
     * Returns -1 if there has not been a state change yet. */
    fun stateTimeMs(): Long {
        val nowMs = clock.elapsedRealtime()
        return if (stateTS == 0L) -1 else nowMs - stateTS
    }

    /** Initializes the underlying JMRI sensor. */
    abstract override fun onExecStart()

    abstract override fun onExecHandle()

    /**
     * Block equality purely relies on the equality of the unique systemName ID.
     * The active state of the block is NOT used in equality. This ensures stability
     * when used as a map key.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IBlock) return false
        return systemName == other.systemName
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
        return if (lastActive) "<$s>" else "{$s}"
    }

    abstract override fun toSimulRouteBlock(reversal: Boolean?): SimulRouteBlock
}
