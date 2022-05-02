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

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class BooleanCacheTest {

    @Before
    fun setUp() {
    }

    @Test
    fun testGet() {
        val bc = BooleanCache<Key>()
        val k1 = Key()
        val k2 = Key()
        val k3 = Key()
        val k4 = Key()

        bc.put(k1, true)
        bc.put(k2, false)
        bc.put(k4, true)

        assertThat(bc.get(k1)).isTrue()
        assertThat(bc.get(k2)).isFalse()
        assertThat(bc.get(k3)).isFalse()
        assertThat(bc.get(k4)).isTrue()

        bc.remove(k4)
        assertThat(bc.get(k4)).isFalse()
    }

    @Test
    fun testGetOrEval() {
        val bc = BooleanCache<Key>()
        val k1 = Key()
        val k2 = Key()
        val k3 = Key()
        val k4 = Key()

        bc.put(k1, true)
        bc.put(k3, false)

        val eval1 = Eval(false)
        val eval2 = Eval(true)
        val eval3 = Eval(false)
        val eval4 = Eval(true)

        assertThat(bc.getOrEval(k1, eval1::evaluator)).isTrue()     // cached
        assertThat(bc.getOrEval(k2, eval2::evaluator)).isTrue()     // evaluated
        assertThat(bc.getOrEval(k3, eval3::evaluator)).isFalse()    // cached
        assertThat(bc.getOrEval(k4, eval4::evaluator)).isTrue()     // evaluated

        assertThat(eval1.evaluated).isFalse()
        assertThat(eval2.evaluated).isTrue()
        assertThat(eval3.evaluated).isFalse()
        assertThat(eval4.evaluated).isTrue()
    }

    internal class Key

    internal class Eval(private val expected: Boolean) {
        var evaluated = false

        fun evaluator() : Boolean {
            evaluated = true
            return expected
        }
    }
}
