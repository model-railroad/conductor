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

package com.alfray.conductor.v2.script.impl

import com.alflabs.conductor.jmri.IJmriProvider
import com.alflabs.conductor.jmri.IJmriThrottle
import com.alflabs.conductor.util.EventLogger
import com.alflabs.kv.IKeyValue
import com.alflabs.utils.ILogger
import com.alflabs.utils.MockClock
import com.alfray.conductor.v2.script.dsl.DccSpeed
import com.alfray.conductor.v2.script.dsl.Delay
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import dagger.internal.InstanceFactory
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class ThrottleTest {
    private val jmriThrottle: IJmriThrottle = mock { on { dccAddress } doReturn 42 }
    private val jmriProvider: IJmriProvider = mock { on { getThrottle(42) } doReturn jmriThrottle }
    private val eventLogger: EventLogger = mock()
    private val keyValue: IKeyValue = mock()
    private val logger: ILogger = mock()
    private val clock = MockClock()
    private lateinit var throttle: Throttle

    @Before
    fun setUp() {
        val factory = Throttle_Factory(
            InstanceFactory.create(clock),
            InstanceFactory.create(logger),
            InstanceFactory.create(keyValue),
            InstanceFactory.create(eventLogger),
            InstanceFactory.create(jmriProvider))
        throttle = factory.get(42)

        assertThat(throttle.dccAddress).isEqualTo(42)

        throttle.onExecStart()
        verify(jmriProvider).getThrottle(42)
        verify(keyValue).putValue("D/42", "0", true)
        reset(keyValue)
    }

    @Test
    fun testInit() {
        assertThat(throttle.forward).isFalse()
        assertThat(throttle.reverse).isFalse()
        assertThat(throttle.stopped).isTrue()
    }

    @Test
    fun testForward() {
        throttle.forward(DccSpeed(41))
        verify(jmriThrottle).setSpeed(41)
        verify(keyValue).putValue("D/42", "41", true)
        assertThat(throttle.forward).isTrue()
        assertThat(throttle.reverse).isFalse()
        assertThat(throttle.stopped).isFalse()
        // Contrary to Conductor1, forward(negative value) means going reverse.
        throttle.forward(DccSpeed(-43))
        verify(jmriThrottle).setSpeed(-43)
        verify(keyValue).putValue("D/42", "-43", true)
        assertThat(throttle.forward).isFalse()
        assertThat(throttle.reverse).isTrue()
        assertThat(throttle.stopped).isFalse()
        verify(jmriThrottle, atLeastOnce()).dccAddress
    }

    @Test
    fun testReverse() {
        throttle.reverse(DccSpeed(41))
        verify(jmriThrottle).setSpeed(-41)
        verify(keyValue).putValue("D/42", "-41", true)
        assertThat(throttle.forward).isFalse()
        assertThat(throttle.reverse).isTrue()
        assertThat(throttle.stopped).isFalse()
        // Contrary to Conductor1, reverse(positive value) means going forward.
        throttle.reverse(DccSpeed(-43))
        verify(jmriThrottle).setSpeed(43)
        verify(keyValue).putValue("D/42", "43", true)
        assertThat(throttle.forward).isTrue()
        assertThat(throttle.reverse).isFalse()
        assertThat(throttle.stopped).isFalse()
        verify(jmriThrottle, atLeastOnce()).dccAddress
    }

    @Test
    fun testStop() {
        throttle.forward(DccSpeed(41))
        verify(jmriThrottle).setSpeed(41)
        verify(keyValue).putValue("D/42", "41", true)
        throttle.stop()
        verify(jmriThrottle).setSpeed(0)
        verify(keyValue).putValue("D/42", "0", true)
        assertThat(throttle.forward).isFalse()
        assertThat(throttle.reverse).isFalse()
        assertThat(throttle.stopped).isTrue()
        verify(jmriThrottle, atLeastOnce()).dccAddress
    }

    @Test
    fun testSound() {
        assertThat(throttle.sound).isFalse()
        throttle.sound(false)
        assertThat(throttle.sound).isFalse()
        verify(jmriThrottle).setSound(false)
        throttle.sound(true)
        assertThat(throttle.sound).isTrue()
        verify(jmriThrottle).setSound(true)
        reset(jmriThrottle)
        throttle.sound(false)
        assertThat(throttle.sound).isFalse()
        verify(jmriThrottle).setSound(false)
    }

    @Test
    fun testLight() {
        assertThat(throttle.light).isFalse()
        throttle.light(false)
        assertThat(throttle.light).isFalse()
        verify(jmriThrottle).setLight(false)
        throttle.light(true)
        assertThat(throttle.light).isTrue()
        verify(jmriThrottle).setLight(true)
        reset(jmriThrottle)
        throttle.light(false)
        assertThat(throttle.light).isFalse()
        verify(jmriThrottle).setLight(false)
    }

    @Test
    fun testFnFunction() {
        throttle.f(3, true)
        verify(jmriThrottle).triggerFunction(3, true)
        throttle.f(5, false)
        verify(jmriThrottle).triggerFunction(5, false)
        throttle.f(12, true)
        verify(jmriThrottle).triggerFunction(12, true)
        throttle.f(28, false)
        verify(jmriThrottle).triggerFunction(28, false)
    }

    @Test
    fun testRepeatFunction() {
        assertThat(throttle._repeatSpeedSeconds).isEqualTo(Delay(0))
        throttle.repeat(Delay(0))
        assertThat(throttle._repeatSpeedSeconds).isEqualTo(Delay(0))
        throttle.repeat(Delay(2))
        assertThat(throttle._repeatSpeedSeconds).isEqualTo(Delay(2))
        throttle.repeat(Delay(0))
        assertThat(throttle._repeatSpeedSeconds).isEqualTo(Delay(0))
    }

    @Test
    fun testRepeatSpeed() {
        throttle.repeat(Delay(1))
        throttle.forward(DccSpeed(41))
        throttle.repeatSpeed()
        verify(jmriThrottle, times(1)).setSpeed(41)
        verify(keyValue, times(1)).putValue("D/42", "41", true)
        clock.sleep(500)
        throttle.repeatSpeed()
        verify(jmriThrottle, times(1)).setSpeed(41)
        clock.sleep(500)
        throttle.repeatSpeed()
        verify(jmriThrottle, times(2)).setSpeed(41)
        verify(keyValue, times(2)).putValue("D/42", "41", true)
        reset(jmriThrottle)
        reset(keyValue)
    }
}
