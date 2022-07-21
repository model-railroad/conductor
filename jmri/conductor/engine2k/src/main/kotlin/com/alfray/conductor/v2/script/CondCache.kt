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

package com.alfray.conductor.v2.script

import com.alfray.conductor.v2.dagger.Script2kScope
import com.alfray.conductor.v2.script.dsl.DccSpeed
import javax.inject.Inject

/**
 * Condition cache to memorize the state of JMRI inputs during one execution of the
 * ExecEngine loop. As inputs are checked, their first value is cached and reused to
 * guarantee consistency during evaluation of rules' conditions.
 * <p/>
 * The cache has a "frozen" state:
 * - When "frozen", values are cached the first time they are queried and then the
 *   "frozen" cached value is served for future calls.
 * - When not "frozen", the cache is cleared and values are served as-is. In that case
 *   the cache is a pass-through.
 */
@Script2kScope
class CondCache @Inject constructor() {
    private var frozen: Boolean = false
    private val states = mutableMapOf<String, Boolean>()
    private val speeds = mutableMapOf<String, DccSpeed>()

    fun freeze() {
        if (!frozen) {
            states.clear()
            speeds.clear()
            frozen = true
        }
    }

    fun unfreeze() {
        if (frozen) {
            states.clear()
            speeds.clear()
            frozen = false
        }
    }

    fun cached(state: Boolean, keyName: String, subkey: String = ""): Boolean {
        if (!frozen) {
            return state
        }
        val key = if (subkey.isEmpty()) keyName else "$keyName:$subkey"
        return states.getOrPut(key) { state }
    }

    fun cachedSpeed(speed: DccSpeed, keyName: String): DccSpeed {
        if (!frozen) {
            return speed
        }
        return speeds.getOrPut(keyName) { speed }
    }
}
