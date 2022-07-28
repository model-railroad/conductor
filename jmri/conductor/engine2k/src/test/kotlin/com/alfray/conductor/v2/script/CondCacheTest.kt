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

import com.alfray.conductor.v2.script.dsl.DccSpeed
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

class CondCacheTest: ScriptTest2kBase() {

    @Inject lateinit var condCache: CondCache

    @Before
    fun setUp() {
        createComponent()
        scriptComponent.inject(this)
        assertThat(condCache).isNotNull()
    }

    @Test
    fun testCached() {
        condCache.freeze()
        assertThat(condCache.cached(false, "NameA")).isFalse()
        assertThat(condCache.cached(true,  "NameA")).isFalse()
        assertThat(condCache.cached(false, "NameA", "2")).isFalse()
        assertThat(condCache.cached(true,  "NameA", "2")).isFalse()
        assertThat(condCache.cached(true,  "NameB")).isTrue()
        assertThat(condCache.cached(false, "NameB")).isTrue()

        condCache.unfreeze()
        assertThat(condCache.cached(true,  "NameA")).isTrue()
        assertThat(condCache.cached(true,  "NameA", "2")).isTrue()
        assertThat(condCache.cached(false, "NameB")).isFalse()
    }

    @Test
    fun testCachedSpeed() {
        condCache.freeze()
        assertThat(condCache.cachedSpeed(DccSpeed(5), "NameA")).isEqualTo(DccSpeed(5))
        assertThat(condCache.cachedSpeed(DccSpeed(0), "NameA")).isEqualTo(DccSpeed(5))
        condCache.unfreeze()
        assertThat(condCache.cachedSpeed(DccSpeed(0), "NameA")).isEqualTo(DccSpeed(0))
    }
}
