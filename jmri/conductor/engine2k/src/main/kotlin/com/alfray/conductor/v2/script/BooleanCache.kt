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

internal typealias TBoolean = () -> Boolean

internal class BooleanCache<in T> {
    private val mCache = mutableMapOf<T, Boolean>()

    fun clear() {
        mCache.clear()
    }

    fun put(key: T, status: Boolean) {
        mCache[key] = status
    }

    fun remove(key: T) {
        mCache.remove(key)
    }

    fun get(key: T): Boolean = mCache.getOrDefault(key, false)

    fun getOrEval(key: T, eval: TBoolean): Boolean = mCache.getOrPut(key, eval)
}
