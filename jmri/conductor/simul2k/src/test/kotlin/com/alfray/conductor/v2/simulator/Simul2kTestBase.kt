package com.alfray.conductor.v2.simulator

import com.alflabs.conductor.jmri.IJmriProvider
import com.alflabs.utils.FakeClock
import javax.inject.Inject


open class Simul2kTestBase {
    @Inject protected lateinit var jmriProvider: IJmriProvider
    @Inject protected lateinit var simul2k: Simul2k
    @Inject protected lateinit var clock: FakeClock
    protected lateinit var component: ISimul2kTestComponent

    fun createComponent(): ISimul2kTestComponent {
        component = DaggerISimul2kTestComponent
            .factory()
            .createComponent()
        component.inject(this)
        return component
    }
}
