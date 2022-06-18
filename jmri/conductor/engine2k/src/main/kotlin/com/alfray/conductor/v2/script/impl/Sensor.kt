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
import com.alfray.conductor.v2.dagger.Script2kScope
import com.alfray.conductor.v2.script.dsl.ISensor
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

@Script2kScope
@AssistedFactory
internal interface ISensorFactory {
    fun create(systemName: String) : Sensor
}

internal class Sensor @AssistedInject constructor(
    private val keyValue: IKeyValue,
    private val eventLogger: EventLogger,
    private val jmriProvider: IJmriProvider,
    @Assisted override val systemName: String
) : ISensor, IExecEngine {
    private var jmriSensor: IJmriSensor? = null
    private var _active = false
    private var lastActive = false
    private val keyName = "${Prefix.Sensor}$systemName"

    override val active: Boolean
        get() {
            jmriSensor?.let { _active = it.isActive }
            return _active
        }

    override fun not(): Boolean = !active

    override fun active(isActive: Boolean) {
        _active = isActive
        jmriSensor?.isActive = isActive
    }

    override fun onExecStart() {
        jmriSensor = checkNotNull(jmriProvider.getSensor(systemName))
        onExecHandle()
    }

    override fun onExecHandle() {
        val a = active
        val value = if (a) Constants.On else Constants.Off
        keyValue.putValue(keyName, value, true /*broadcast*/)
        if (a != lastActive) {
            lastActive = a
            eventLogger.logAsync(EventLogger.Type.Sensor, keyName, value)
        }
    }
}
