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

import com.alflabs.conductor.dagger.CommonTestModule
import com.alflabs.conductor.jmri.FakeJmriProvider
import com.alflabs.conductor.jmri.IJmriProvider
import com.alfray.conductor.v2.dagger.IEngine2kComponent
import com.alfray.conductor.v2.dagger.Script2kContext
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import junit.framework.TestCase
import javax.inject.Inject
import javax.inject.Singleton

class Script2kLoaderTest : TestCase() {

    private val jmriProvider = FakeJmriProvider()
    private lateinit var component: LocalComponent2k
    @Inject internal lateinit var context: Script2kContext

    public override fun setUp() {
        component = DaggerScript2kLoaderTest_LocalComponent2k
            .factory()
            .createComponent(jmriProvider)
        component.inject(this)
        assertThat(context).isNotNull()
    }

    override fun tearDown() {
        context.reset()
    }

    fun testLoadEmptyScript() {
        val scriptComponent = context.createComponent()
        assertThat(context.script2kComponent.isPresent).isTrue()

        val script2kLoader = scriptComponent.script2kLoader
        assertThat(script2kLoader).isNotNull()

        script2kLoader.loadScriptFromText(scriptText = "")
        assertThat(script2kLoader.conductorImpl).isNotNull()
        assertThat(script2kLoader.execEngine).isNotNull()
    }

    @Singleton
    @Component(modules = [CommonTestModule::class])
    internal interface LocalComponent2k : IEngine2kComponent {
        fun inject(test: Script2kLoaderTest)

        @Component.Factory
        interface Factory {
            fun createComponent(@BindsInstance jmriProvider: IJmriProvider): LocalComponent2k
        }
    }
}
