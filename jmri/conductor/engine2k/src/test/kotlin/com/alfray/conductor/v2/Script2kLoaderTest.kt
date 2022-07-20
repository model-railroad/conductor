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

package com.alfray.conductor.v2

import com.alflabs.conductor.jmri.FakeJmriProvider
import com.alfray.conductor.v2.dagger.DaggerITestComponent2k
import com.alfray.conductor.v2.dagger.ITestComponent2k
import com.alfray.conductor.v2.dagger.Script2kContext
import com.alfray.conductor.v2.dagger.Script2kTestContext
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

class Script2kLoaderTest {

    private val jmriProvider = FakeJmriProvider()
    private lateinit var component: ITestComponent2k
    @Inject internal lateinit var context: Script2kTestContext

    @Before
    fun setUp() {
        component = DaggerITestComponent2k
            .factory()
            .createComponent(jmriProvider)
        component.inject(this)
        assertThat(context).isNotNull()
    }

    @After
    fun tearDown() {
        context.reset()
    }

    @Test
    fun testLoadEmptyScript() {
        val scriptComponent = context.createTestComponent()
        assertThat(context.script2kComponent.isPresent).isTrue()

        val script2kLoader = scriptComponent.script2kLoader
        assertThat(script2kLoader).isNotNull()

        script2kLoader.loadScriptFromText(scriptText = "")
        assertThat(script2kLoader.conductorImpl).isNotNull()
        assertThat(script2kLoader.execEngine).isNotNull()
    }
}
