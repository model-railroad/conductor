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
import com.alfray.conductor.v2.dagger.Script2kScope
import com.alfray.conductor.v2.script.dsl.ITurnout
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

@Script2kScope
@AssistedFactory
internal interface ITurnoutFactory {
    fun create(systemName: String) : Turnout
}

internal class Turnout @AssistedInject constructor(
    private val jmriProvider: IJmriProvider,
    @Assisted override val systemName: String
) : ITurnout, IExecEngine {
    private lateinit var jmriTurnout: IJmriTurnout
    private var _normal = true

    override val normal: Boolean
        get() = _normal

    override val active: Boolean
        get() = _normal

    override fun not(): Boolean = !active

    override fun normal() {
        _normal = true
    }

    override fun reverse() {
        _normal = false
    }

    override fun onExecStart() {
        jmriTurnout = checkNotNull(jmriProvider.getTurnout(systemName))
    }

    override fun onExecHandle() {
        TODO("Not yet implemented")
    }
}
