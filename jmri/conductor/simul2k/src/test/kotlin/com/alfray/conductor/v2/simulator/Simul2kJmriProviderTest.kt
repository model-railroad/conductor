package com.alfray.conductor.v2.simulator

import com.alflabs.conductor.jmri.FakeJmriProvider
import com.alflabs.conductor.jmri.IJmriProvider
import com.alflabs.utils.FakeClock
import com.alflabs.utils.IClock
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

class Simul2kJmriProviderTest {

    private lateinit var component: ISimul2kTestComponent
    @Inject internal lateinit var jmriProvider: IJmriProvider
    @Inject internal lateinit var clock: IClock

    @Before
    fun setUp() {
        component = DaggerISimul2kTestComponent
            .factory()
            .createComponent()
        component.inject(this)
    }

    @Test
    fun basicTest() {
        assertThat(jmriProvider).isInstanceOf(FakeJmriProvider::class.java)
        assertThat(clock).isInstanceOf(FakeClock::class.java)

        (clock as FakeClock).setNow(42)
        assertThat(component.getJmriProvider()).isSameInstanceAs(jmriProvider)
    }
}
