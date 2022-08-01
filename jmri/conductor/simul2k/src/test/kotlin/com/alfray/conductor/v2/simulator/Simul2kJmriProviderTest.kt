package com.alfray.conductor.v2.simulator

import com.alflabs.conductor.jmri.FakeJmriProvider
import com.alflabs.conductor.jmri.IJmriProvider
import com.alflabs.utils.FakeClock
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

class Simul2kJmriProviderTest : Simul2kTestBase() {

    @Inject internal lateinit var jmriProvider2: IJmriProvider

    @Before
    fun setUp() {
        createComponent().inject(this)
    }

    @Test
    fun testInjection() {
        assertThat(jmriProvider).isInstanceOf(FakeJmriProvider::class.java)
        assertThat(component.getJmriProvider()).isSameInstanceAs(jmriProvider)
        assertThat(jmriProvider2).isSameInstanceAs(jmriProvider)

        assertThat(clock).isInstanceOf(FakeClock::class.java)
        clock.setNow(42)
    }
}
