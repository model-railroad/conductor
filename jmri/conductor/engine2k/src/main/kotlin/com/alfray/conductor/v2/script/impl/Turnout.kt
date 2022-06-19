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
import com.alflabs.conductor.jmri.IJmriTurnout
import com.alflabs.kv.IKeyValue
import com.alflabs.manifest.Constants
import com.alflabs.manifest.Prefix
import com.alfray.conductor.v2.dagger.Script2kScope
import com.alfray.conductor.v2.script.dsl.ITurnout
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

/** Creates a new turnout for the given JMRI system name. */
@Script2kScope
@AssistedFactory
internal interface ITurnoutFactory {
    fun create(systemName: String) : Turnout
}

/**
 * A turnout defined by a script.
 * <p/>
 * The actual JMRI turnout is only assigned via the [onExecStart] method.
 * The actual JMRI turnout state is read in [onExecHandle].
 * <p/>
 * Querying the active state returns the internal state and does NOT read from JMRI;
 * reading from JMRI is only done in [onExecHandle] to guarantee to be in sync with the exec loop.
 * <p/>
 * When used as a conditional, a turnout is true in its "normal" state and false
 * in reverse.
 */
internal class Turnout @AssistedInject constructor(
    private val keyValue: IKeyValue,
    private val jmriProvider: IJmriProvider,
    @Assisted override val systemName: String
) : ITurnout, IExecEngine {
    private var jmriTurnout: IJmriTurnout? = null
    /** Internal turnout state. This is never updated when reading [active]. Instead, the
     * internal state is updated when set by the script or in [onExecHandle] to guarantee
     * to be in sync with the exec loop. */
    private var _normal = true
    private val keyName = "${Prefix.Turnout}$systemName"

    override val normal: Boolean
        get() = _normal

    override val active: Boolean
        get() = _normal // uses internal state, does NOT update from JMRI.

    override fun not(): Boolean = !active

    override fun normal() {
        setTurnout(normal = true)
    }

    override fun reverse() {
        setTurnout(normal = false)
    }

    private fun setTurnout(normal: Boolean) {
        // Updates the internal state and the JMRI state.
        _normal = normal
        jmriTurnout?.setTurnout(if (_normal) IJmriTurnout.NORMAL else IJmriTurnout.REVERSE)
    }

    override fun onExecStart() {
        jmriTurnout = checkNotNull(jmriProvider.getTurnout(systemName))
        onExecHandle()
    }

    override fun onExecHandle() {
        // Read the JMRI state.
        jmriTurnout?.let { _normal = it.isNormal }
        keyValue.putValue(
            keyName,
            if (_normal) Constants.Normal else Constants.Reverse,
            true /*broadcast*/
        )

    }
}
