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
import com.alflabs.conductor.util.DazzSender
import com.alflabs.conductor.util.EventLogger
import com.alflabs.conductor.util.JsonSender
import com.alflabs.kv.IKeyValue
import com.alflabs.utils.MockClock
import com.alfray.conductor.v2.dagger.Script2kIsSimulation
import com.alfray.conductor.v2.script.CondCache
import com.alfray.conductor.v2.script.CurrentContext
import com.alfray.conductor.v2.script.ExecContext
import com.alfray.conductor.v2.script.ScriptTest2kBase
import com.alfray.conductor.v2.script.dsl.DccSpeed
import com.alfray.conductor.v2.script.dsl.Delay
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.text.DateFormat
import javax.inject.Inject

class ThrottleTest : ScriptTest2kBase() {
    @Inject internal lateinit var currentContext: CurrentContext
    @Inject internal lateinit var isSimulation: Script2kIsSimulation
    private val testContext = ExecContext(ExecContext.Reason.GLOBAL_RULE)

    private val mockThrottle = mock<IJmriThrottle> { on { dccAddress } doReturn 42 }
    private val mockProvider = mock<IJmriProvider> { on { getThrottle(42) } doReturn mockThrottle }
    private val jsonSender = mock<JsonSender>()
    private val dazzSender = mock<DazzSender>()
    private val eventLogger = mock<EventLogger>()
    private val jsonDateFormat = mock<DateFormat>()
    private val keyValue = mock<IKeyValue>()
    private val mockClock = MockClock()
    private val condCache = CondCache()
    private lateinit var throttle: Throttle

    @Before
    fun setUp() {
        createComponent()
        scriptComponent.inject(this)

        val factory = Factory(
            mockClock,
            logger,
            keyValue,
            condCache,
            jsonSender,
            dazzSender,
            eventLogger,
            mockProvider,
            currentContext,
            isSimulation,
            { mock<ThrottleBuilder>() },
            jsonDateFormat,
            { mock<RoutesContainerBuilder>() },
        )
        throttle = factory.createThrottle(42)

        assertThat(throttle.dccAddress).isEqualTo(42)
        currentContext.changeContext(testContext)

        throttle.onExecStart()
        verify(mockProvider).getThrottle(42)
        verify(keyValue).putValue("D/42", "0", true)
        reset(keyValue)
    }

    @After
    fun tearDown() {
        currentContext.resetContext()
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
        verify(mockThrottle).setSpeed(41)
        verify(keyValue).putValue("D/42", "41", true)
        verify(eventLogger).logAsync(EventLogger.Type.DccThrottle, "42", "41")
        assertThat(throttle.forward).isTrue()
        assertThat(throttle.reverse).isFalse()
        assertThat(throttle.stopped).isFalse()
        // Contrary to Conductor1, forward(negative value) means going reverse.
        throttle.forward(DccSpeed(-43))
        verify(mockThrottle).setSpeed(-43)
        verify(keyValue).putValue("D/42", "-43", true)
        verify(eventLogger).logAsync(EventLogger.Type.DccThrottle, "42", "-43")
        assertThat(throttle.forward).isFalse()
        assertThat(throttle.reverse).isTrue()
        assertThat(throttle.stopped).isFalse()
    }

    @Test
    fun testReverse() {
        throttle.reverse(DccSpeed(41))
        verify(mockThrottle).setSpeed(-41)
        verify(keyValue).putValue("D/42", "-41", true)
        verify(eventLogger).logAsync(EventLogger.Type.DccThrottle, "42", "-41")
        assertThat(throttle.forward).isFalse()
        assertThat(throttle.reverse).isTrue()
        assertThat(throttle.stopped).isFalse()
        // Contrary to Conductor1, reverse(positive value) means going forward.
        throttle.reverse(DccSpeed(-43))
        verify(mockThrottle).setSpeed(43)
        verify(keyValue).putValue("D/42", "43", true)
        verify(eventLogger).logAsync(EventLogger.Type.DccThrottle, "42", "43")
        assertThat(throttle.forward).isTrue()
        assertThat(throttle.reverse).isFalse()
        assertThat(throttle.stopped).isFalse()
    }

    @Test
    fun testStop() {
        throttle.forward(DccSpeed(41))
        verify(mockThrottle).setSpeed(41)
        verify(keyValue).putValue("D/42", "41", true)
        throttle.stop()
        verify(mockThrottle).setSpeed(0)
        verify(keyValue).putValue("D/42", "0", true)
        verify(eventLogger).logAsync(EventLogger.Type.DccThrottle, "42", "0")
        assertThat(throttle.forward).isFalse()
        assertThat(throttle.reverse).isFalse()
        assertThat(throttle.stopped).isTrue()
    }

    @Test
    fun testSound() {
        assertThat(throttle.sound).isFalse()
        throttle.sound(false)
        assertThat(throttle.sound).isFalse()
        verify(mockThrottle).setSound(false)
        verify(eventLogger).logAsync(EventLogger.Type.DccThrottle, "42", "Sound OFF")

        throttle.sound(true)
        assertThat(throttle.sound).isTrue()
        verify(mockThrottle).setSound(true)
        verify(eventLogger).logAsync(EventLogger.Type.DccThrottle, "42", "Sound ON")

        reset(mockThrottle)
        throttle.sound(false)
        assertThat(throttle.sound).isFalse()
        verify(mockThrottle).setSound(false)
    }

    @Test
    fun testLight() {
        assertThat(throttle.light).isFalse()
        throttle.light(false)
        assertThat(throttle.light).isFalse()
        verify(mockThrottle).setLight(false)
        verify(eventLogger).logAsync(EventLogger.Type.DccThrottle, "42", "Light OFF")

        throttle.light(true)
        assertThat(throttle.light).isTrue()
        verify(mockThrottle).setLight(true)
        verify(eventLogger).logAsync(EventLogger.Type.DccThrottle, "42", "Light ON")

        reset(mockThrottle)
        throttle.light(false)
        assertThat(throttle.light).isFalse()
        verify(mockThrottle).setLight(false)
    }

    @Test
    fun testFnFunction() {
        throttle.f(3, true)
        verify(mockThrottle).triggerFunction(3, true)
        verify(eventLogger).logAsync(EventLogger.Type.DccThrottle, "42", "F3 ON")
        throttle.f(5, false)
        verify(mockThrottle).triggerFunction(5, false)
        verify(eventLogger).logAsync(EventLogger.Type.DccThrottle, "42", "F5 OFF")
        throttle.f(12, true)
        verify(mockThrottle).triggerFunction(12, true)
        verify(eventLogger).logAsync(EventLogger.Type.DccThrottle, "42", "F12 ON")
        throttle.f(28, false)
        verify(mockThrottle).triggerFunction(28, false)
        verify(eventLogger).logAsync(EventLogger.Type.DccThrottle, "42", "F28 OFF")
    }

    @Test
    fun testRepeatFunction() {
        assertThat(throttle.repeatSpeedSeconds).isEqualTo(Delay(0))
        throttle.repeat(Delay(0))
        assertThat(throttle.repeatSpeedSeconds).isEqualTo(Delay(0))
        throttle.repeat(Delay(2))
        assertThat(throttle.repeatSpeedSeconds).isEqualTo(Delay(2))
        throttle.repeat(Delay(0))
        assertThat(throttle.repeatSpeedSeconds).isEqualTo(Delay(0))
    }

    @Test
    fun testRepeatSpeed() {
        throttle.repeat(Delay(1))
        throttle.forward(DccSpeed(41))
        throttle.repeatSpeed()
        verify(mockThrottle, times(1)).setSpeed(41)
        verify(keyValue, times(1)).putValue("D/42", "41", true)
        mockClock.sleep(500)
        throttle.repeatSpeed()
        verify(mockThrottle, times(1)).setSpeed(41)
        mockClock.sleep(500)
        throttle.repeatSpeed()
        verify(mockThrottle, times(2)).setSpeed(41)
        verify(keyValue, times(2)).putValue("D/42", "41", true)
        reset(mockThrottle)
        reset(keyValue)
    }
}
