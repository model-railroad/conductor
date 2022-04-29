package com.alfray.conductor.v2

import com.alflabs.conductor.dagger.CommonModule
import com.alflabs.conductor.dagger.CommonTestModule
import com.alflabs.conductor.jmri.FakeJmriProvider
import com.alflabs.conductor.jmri.IJmriProvider
import com.alfray.conductor.v2.dagger.IEngine2kComponent
import com.alfray.conductor.v2.dagger.IScript2kComponent
import com.alfray.conductor.v2.dagger.Script2kContext
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import junit.framework.TestCase
import java.util.*
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
        val scriptComponent = context.script2kCompFactory.createComponent()
        assertThat(scriptComponent).isNotNull()
        context.script2kComponent = Optional.of(scriptComponent)

        val script2kLoader = scriptComponent.script2kLoader
        assertThat(script2kLoader).isNotNull()

        script2kLoader.loadScriptFromText(scriptText = "")
        assertThat(script2kLoader.conductorImpl).isNotNull()
        assertThat(script2kLoader.execEngine).isNotNull()
        assertThat(script2kLoader.conductorOptional().isPresent).isTrue()
        assertThat(script2kLoader.execEngineOptional().isPresent).isTrue()
    }

    @Singleton
    @Component(modules = [CommonTestModule::class])
    internal interface LocalComponent2k : IEngine2kComponent {
        val scriptComponentFactory: IScript2kComponent.Factory

        fun inject(test: Script2kLoaderTest)

        @Component.Factory
        interface Factory {
            fun createComponent(@BindsInstance jmriProvider: IJmriProvider): LocalComponent2k
        }
    }
}
