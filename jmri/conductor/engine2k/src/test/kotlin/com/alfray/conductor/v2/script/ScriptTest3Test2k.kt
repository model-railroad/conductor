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

import com.alflabs.conductor.dagger.FakeEventLogger
import com.alflabs.kv.IKeyValue
import com.alflabs.utils.FakeClock
import com.alfray.conductor.v2.script.impl.Block
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

/** Tests using script_test3.conductor.kts file. */
class ScriptTest3Test2k : ScriptTest2kBase() {
    @Inject lateinit var clockMillis: FakeClock
    @Inject lateinit var keyValue: IKeyValue
    @Inject lateinit var eventLogger: FakeEventLogger

    @Before
    fun setUp() {
        createComponent()
        scriptComponent.inject(this)
    }

    @Test
    fun testScript3() {
        loadScriptFromFile("script_test3")
        assertResultNoError()

        val mlToggle = conductorImpl.sensors["NS829"]!!
        val b311 = conductorImpl.blocks["NS769"]!! as Block

        assertThat(eventLogger.eventLogGetAndClear()).containsExactly(
            "<clock 1000> - S - S/NS769 B311 - OFF",
            "<clock 1000> - S - S/NS771 B321 - OFF",
            "<clock 1000> - R - Idle Mainline #0 ML Ready - ACTIVATED"
        ).inOrder()

        // Engine starts on B311
        b311.internalActive(true)

        // Simulate about 1 second.
        repeat(10) {
            clockMillis.add(100)
            execEngine.onExecHandle()
        }
        assertThat(eventLogger.eventLogGetAndClear()).containsExactly(
            "<clock 1100> - S - S/NS769 B311 - ON",
            "<clock 1100> - D - 1072 - Light ON",
            "<clock 1100> - D - 1072 - Bell OFF",
            "<clock 1100> - D - 1072 - F1 OFF",
            "<clock 1100> - R - Idle Mainline #0 ML Ready - ACTIVE"
        ).inOrder()

        mlToggle.active(true)
        execEngine.onExecHandle()
        assertThat(eventLogger.eventLogGetAndClear()).containsExactly(
            "<clock 2000> - S - S/NS829 ML-Toggle - ON",
            "<clock 2000> - R - Idle Mainline #0 ML Ready - IDLE",
            "<clock 2000> - R - Sequence Mainline #2 Freight (1072) - ACTIVATED"
        ).inOrder()

        // The route takes about 5 minutes, aka 300 seconds, at 0.1sec intervals
        repeat(2*60*10) {
            clockMillis.add(100)
            execEngine.onExecHandle()
        }
        assertThat(eventLogger.eventLogGetAndClear()).containsExactly(
            "<clock 2133> - B - S/NS769 B311 - OCCUPIED",
            "<clock 2133> - R - Sequence Mainline #2 Freight (1072) - ACTIVE",
            "<clock 2233> - D - 1072 - Light ON",
            "<clock 2233> - D - 1072 - Sound ON",
            "<clock 2233> - D - 1072 - F8 OFF",
            "<clock 2333> - T - @timer@2 - start:2",
            "<clock 4333> - T - @timer@2 - activated",
            "<clock 4333> - T - @timer@10 - start:10",
            "<clock 4333> - D - 1072 - Horn",
            "<clock 4333> - D - 1072 - Light ON",
            "<clock 4333> - D - 1072 - Bell OFF",
            "<clock 4333> - D - 1072 - F1 OFF",
            "<clock 4333> - D - 1072 - 2",
            "<clock 14333> - T - @timer@10 - activated",
            "<clock 14333> - D - 1072 - 8",
            "<clock 14333> - D - 1072 - Horn"
        ).inOrder()

    }
}
