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

import com.alfray.conductor.v2.script.dsl.speed
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class ExecEngine2kTest : ScriptTest2kBase() {
    @Before
    fun setUp() {
        createComponent().inject(this)
    }

    @Test
    fun testRuleExecCache() {
        loadScriptFromText(
        scriptText =
        """
        val Train1  = throttle(1001)
        val Sensor1 = sensor("S01")
        var n = 5
        on { Sensor1.active } then { Train1.forward(n.speed) ; n = n+5 }
        """.trimIndent()
        )
        assertResultNoError()

        assertThat(conductorImpl.rules).hasSize(1)
        val t = conductorImpl.throttles[1001]!!
        val s = conductorImpl.sensors["S01"]!!

        assertThat(s.active).isFalse()
        assertThat(t.speed).isEqualTo(0.speed)

        // No change when sensor is inactive
        execEngine.onExecHandle()
        assertThat(t.speed).isEqualTo(0.speed)

        // Change *only once* when sensor becomes active.
        s.active(true)
        execEngine.onExecHandle()
        assertThat(t.speed).isEqualTo(5.speed)

        // Further executions ignore this rule as the conditions has not reset yet.
        execEngine.onExecHandle()
        execEngine.onExecHandle()
        assertThat(t.speed).isEqualTo(5.speed)

        // Sensor becomes inactive, which resets the condition.
        s.active(false)
        execEngine.onExecHandle()
        execEngine.onExecHandle()
        assertThat(t.speed).isEqualTo(5.speed)

        // Next invocation thus executes the rule again.
        s.active(true)
        execEngine.onExecHandle()
        execEngine.onExecHandle()
        assertThat(t.speed).isEqualTo(10.speed)
    }

}
