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
 * Tests the syntax of the various script_vAA.BB.conductor.kts files.
 * where "AA" is a Major version number (new logic) and "BB" is a Minor version number (timings).
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
            fileOps.toFile("v2", "script", "src", "test", "resources", "v2", "Conductor Map Mainline 1.svg"))
        fileOps.writeBytes(
            "<svg/>".toByteArray(Charsets.UTF_8),
            fileOps.toFile("v2", "script", "src", "test", "resources", "v2", "Conductor Map Mainline 2.svg"))
    }

    @Test
    fun testScript10() {
        loadScriptFromFile("script_v10.02.conductor.kts")
        assertResultNoError()
    }

    @Test
    fun testScript12() {
        loadScriptFromFile("script_v12.03.conductor.kts")
        assertResultNoError()
    }

    @Test
    fun testScript15() {
        loadScriptFromFile("script_v15.06_bl204+ml722+1067.conductor.kts")
        assertResultNoError()
    }

    @Test
    fun testScript26() {
        loadScriptFromFile("script_v26.11_bl204+ml8330+1067+tl6119_sat.conductor.kts")
        assertResultNoError()
    }

    @Test
    fun testScript27() {
        loadScriptFromFile("script_v27.01_bl204+ml8330+1067+tl6119_sat.conductor.kts")
        assertResultNoError()
    }

    @Test
    fun testScript29() {
        loadScriptFromFile("script_v29.04_bl204+ml8330+1225+tl6885.conductor.kts")
        assertResultNoError()
    }

    @Test
    fun testScript30() {
        loadScriptFromFile("script_v30.03_bl204+ml8312+1067+tl6885.conductor.kts")
        assertResultNoError()
    }

    @Test
    fun testScript31() {
        loadScriptFromFile("script_v31.01_bl204+ml8749+1067+tl6885.conductor.kts")
        assertResultNoError()
    }

    @Test
    fun testScript32() {
        loadScriptFromFile("script_v32.01_bl010+ml8749+1067+tl6885.conductor.kts")
        assertResultNoError()
    }

    @Test
    fun testScript33() {
        loadScriptFromFile("script_v33.03_bl010+ml9538+1072+tl6885.conductor.kts")
        assertResultNoError()
    }

    @Test
    fun testScript34() {
        loadScriptFromFile("script_v34.01_bl191+ml9538+1072+tl6885.conductor.kts")
        assertResultNoError()
    }

    @Test
    fun testScript35() {
        loadScriptFromFile("script_v35.01_bl191+ml9538+1072+tl6885.conductor.kts")
        assertResultNoError()
    }
}
