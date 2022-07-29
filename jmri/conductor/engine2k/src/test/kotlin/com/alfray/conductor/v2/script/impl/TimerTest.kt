package com.alfray.conductor.v2.script.impl

import com.alflabs.utils.FakeClock
import com.alfray.conductor.v2.script.ScriptTest2kBase
import com.alfray.conductor.v2.script.dsl.seconds
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

class TimerTest: ScriptTest2kBase() {
    @Inject internal lateinit var clock: FakeClock
    @Inject internal lateinit var factory: Factory
    private lateinit var timer: Timer

    @Before
    fun setUp() {
        createComponent()
        scriptComponent.inject(this)

        clock.setNow(100 * 1000L)
        timer = factory.createTimer(42.seconds)
    }

    @Test
    @Throws(Exception::class)
    fun testTimer() {
        assertThat(timer.active).isFalse()
        timer.start()
        assertThat(timer.active).isFalse()
        clock.setNow(141 * 1000L)
        assertThat(timer.active).isFalse()
        clock.setNow(142 * 1000L)
        assertThat(timer.active).isTrue()

        // timer stays active till reset using end
        clock.setNow(200 * 1000L)
        assertThat(timer.active).isTrue()
        timer.reset()
        assertThat(timer.active).isFalse()
    }

    @Test
    @Throws(Exception::class)
    fun testTimerReset() {
        assertThat(timer.active).isFalse()
        timer.start()
        assertThat(timer.active).isFalse()

        // timer active now
        clock.setNow(200 * 1000L)
        assertThat(timer.active).isTrue()
        timer.reset()
        assertThat(timer.active).isFalse()
    }

    @Test
    fun testTimerNoRestart() {
        // Validates that "start" does not restart a timer that is already ongoing.
        timer.start()
        assertThat(timer.active).isFalse()
        clock.setNow((100 + 41) * 1000L)
        assertThat(timer.active).isFalse()

        // At t+41 s, a new start() should not reset the timer, it should still expire
        // at the original t+42 s.
        timer.start()
        clock.setNow((100 + 42) * 1000L)
        assertThat(timer.active).isTrue()

        // However once the timer has been activated, we can restart it even without calling end.
        timer.start()
        assertThat(timer.active).isFalse()
        clock.setNow((100 + 42 + 41) * 1000L)
        assertThat(timer.active).isFalse()
        clock.setNow((100 + 42 + 42) * 1000L)
        assertThat(timer.active).isTrue()
        timer.reset()
        assertThat(timer.active).isFalse()
    }
}
