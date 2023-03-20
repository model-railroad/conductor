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

import com.alflabs.kv.IKeyValue
import com.alflabs.utils.FakeClock
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

class Scriptv47v2Test2k : ScriptTest2kBase() {
    @Inject lateinit var clock: FakeClock
    @Inject lateinit var keyValue: IKeyValue

    @Before
    fun setUp() {
        createComponent()
        scriptComponent.inject(this)
        fileOps.writeBytes(
            "<svg/>".toByteArray(Charsets.UTF_8),
            fileOps.toFile("v2", "script", "Map 1.svg"))
    }

    @Test
    fun testScript47() {
        fileOps.writeBytes(
            "<svg/>".toByteArray(Charsets.UTF_8),
            fileOps.toFile("v2", "script", "src", "test", "resources", "v2", "Conductor Map Mainline 1.svg"))
        loadScriptFromFile("script_v47_v2")
        assertResultNoError()
    }
}
