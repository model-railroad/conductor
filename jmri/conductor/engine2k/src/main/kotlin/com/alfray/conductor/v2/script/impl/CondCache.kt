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

import com.alfray.conductor.v2.dagger.Script2kScope
import com.alfray.conductor.v2.script.dsl.DccSpeed
import javax.inject.Inject

// TBD @Script2kScope
class CondCache @Inject constructor() {
    private val states = mutableMapOf<String, Boolean>()
    private val speeds = mutableMapOf<String, DccSpeed>()

    fun clear() {
        states.clear()
        speeds.clear()
    }

    fun cached(state: Boolean, keyName: String, subkey: String = ""): Boolean {
        val key = if (subkey.isEmpty()) keyName else "$keyName:$subkey"
        return states.getOrPut(key) { state }
    }

    fun cachedSpeed(speed: DccSpeed, keyName: String): DccSpeed {
        return speeds.getOrPut(keyName) { speed }
    }
}
