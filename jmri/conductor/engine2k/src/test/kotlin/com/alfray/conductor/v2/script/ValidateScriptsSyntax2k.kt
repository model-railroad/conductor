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

/**
 * Tests the syntax of the various script_NN_v2.conductor.kts files.
 * No behavior is actually tested.
 * When adding a new script file, simply add a test here to validate the syntax upfront.
 */
class ValidateScriptsSyntax2k : ScriptTest2kBase() {
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
        loadScriptFromFile("script_47_v2")
        assertResultNoError()
    }

    @Test
    fun testScript53_v2() {
        fileOps.writeBytes(
            "<svg/>".toByteArray(Charsets.UTF_8),
            fileOps.toFile("v2", "script", "src", "test", "resources", "v2", "Conductor Map Mainline 1.svg"))
        loadScriptFromFile("script_53_v2")
        assertResultNoError()
    }

    @Test
    fun testScript53_v3() {
        fileOps.writeBytes(
            "<svg/>".toByteArray(Charsets.UTF_8),
            fileOps.toFile("v2", "script", "src", "test", "resources", "v2", "Conductor Map Mainline 1.svg"))
        loadScriptFromFile("script_53_v3")
        assertResultNoError()
    }

    @Test
    fun testScript54_v1() {
        fileOps.writeBytes(
            "<svg/>".toByteArray(Charsets.UTF_8),
            fileOps.toFile("v2", "script", "src", "test", "resources", "v2", "Conductor Map Mainline 1.svg"))
        loadScriptFromFile("script_54_v1_bl204+ml8312+1072")
        assertResultNoError()
    }

    @Test
    fun testScript54_v2() {
        fileOps.writeBytes(
            "<svg/>".toByteArray(Charsets.UTF_8),
            fileOps.toFile("v2", "script", "src", "test", "resources", "v2", "Conductor Map Mainline 1.svg"))
        loadScriptFromFile("script_54_v2")
        assertResultNoError()
    }

    @Test
    fun testScript54_v3() {
        fileOps.writeBytes(
            "<svg/>".toByteArray(Charsets.UTF_8),
            fileOps.toFile("v2", "script", "src", "test", "resources", "v2", "Conductor Map Mainline 1.svg"))
        loadScriptFromFile("script_54_v3")
        assertResultNoError()
    }

    @Test
    fun testScript54_v4() {
        fileOps.writeBytes(
            "<svg/>".toByteArray(Charsets.UTF_8),
            fileOps.toFile("v2", "script", "src", "test", "resources", "v2", "Conductor Map Mainline 1.svg"))
        loadScriptFromFile("script_54_v4_bl204+ml8330+1072")
        assertResultNoError()
    }

    @Test
    fun testScript54_v5() {
        fileOps.writeBytes(
            "<svg/>".toByteArray(Charsets.UTF_8),
            fileOps.toFile("v2", "script", "src", "test", "resources", "v2", "Conductor Map Mainline 1.svg"))
        loadScriptFromFile("script_54_v5_bl204+ml8312+1067")
        assertResultNoError()
    }

    @Test
    fun testScript54_v6() {
        fileOps.writeBytes(
            "<svg/>".toByteArray(Charsets.UTF_8),
            fileOps.toFile("v2", "script", "src", "test", "resources", "v2", "Conductor Map Mainline 1.svg"))
        loadScriptFromFile("script_54_v6_bl204+ml722+1067")
        assertResultNoError()
    }

    @Test
    fun testScript55_v2() {
        fileOps.writeBytes(
            "<svg/>".toByteArray(Charsets.UTF_8),
            fileOps.toFile("v2", "script", "src", "test", "resources", "v2", "Conductor Map Mainline 1.svg"))
        loadScriptFromFile("script_55_v2_bl204+ml722+5278+tl6885_polar_express")
        assertResultNoError()
    }

    @Test
    fun testScript55_v5() {
        fileOps.writeBytes(
            "<svg/>".toByteArray(Charsets.UTF_8),
            fileOps.toFile("v2", "script", "src", "test", "resources", "v2", "Conductor Map Mainline 1.svg"))
        loadScriptFromFile("script_55_v5_bl204+ml3609+1225+tl6885")
        assertResultNoError()
    }

    @Test
    fun testScript55_v6() {
        fileOps.writeBytes(
            "<svg/>".toByteArray(Charsets.UTF_8),
            fileOps.toFile("v2", "script", "src", "test", "resources", "v2", "Conductor Map Mainline 1.svg"))
        loadScriptFromFile("script_55_v6_bl204+ml3609+1067+tl6885")
        assertResultNoError()
    }
}
