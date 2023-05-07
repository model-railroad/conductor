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
import com.alflabs.conductor.jmri.IJmriTurnout
import com.alflabs.conductor.util.EventLogger
import com.alflabs.kv.IKeyValue
import com.alfray.conductor.v2.script.CondCache
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import dagger.internal.InstanceFactory
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.never

class TurnoutTest {
    private val jmriTurnout = mock<IJmriTurnout> { on { isNormal } doReturn IJmriTurnout.NORMAL }
    private val jmriProvider =
        mock<IJmriProvider> { on { getTurnout("jmriName") } doReturn jmriTurnout }
    private val keyValue = mock<IKeyValue>()
    private val eventLogger = mock<EventLogger>()
    private val condCache = CondCache()
    private lateinit var turnout: Turnout

    @Before
    fun setUp() {
        val factory = Turnout_Factory(
            InstanceFactory.create(keyValue),
            InstanceFactory.create(condCache),
            InstanceFactory.create(eventLogger),
            InstanceFactory.create(jmriProvider)
        )
        turnout = factory.get("jmriName")

        turnout.onExecStart()
        verify(jmriTurnout).isNormal
        reset(jmriTurnout)
        verify(jmriProvider).getTurnout("jmriName")
        verify(keyValue).putValue("T/jmriName", "N", true)
        verifyNoMoreInteractions(eventLogger)
        reset(keyValue)

        assertThat(turnout.active).isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun testNormal() {
        turnout.normal()
        verify(jmriTurnout).setTurnout(IJmriTurnout.NORMAL)
        verify(jmriTurnout, never()).setTurnout(IJmriTurnout.REVERSE)

        jmriTurnout.stub { on { isNormal } doReturn IJmriTurnout.NORMAL }

        verify(keyValue, never())
            .putValue(anyString(), anyString(), anyBoolean())

        turnout.onExecHandle()
        verify(jmriTurnout).isNormal
        verify(keyValue).putValue("T/jmriName", "N", true)
        verify(eventLogger).logAsync(EventLogger.Type.Turnout, "T/jmriName", "normal")
        assertThat(turnout.active).isTrue()
    }

    @Test
    @Throws(Exception::class)
    fun testReverse() {
        turnout.reverse()
        verify(jmriTurnout, never()).setTurnout(IJmriTurnout.NORMAL)
        verify(jmriTurnout).setTurnout(IJmriTurnout.REVERSE)

        jmriTurnout.stub { on { isNormal } doReturn IJmriTurnout.REVERSE }

        verify(keyValue, never())
            .putValue(anyString(), anyString(), anyBoolean())

        turnout.onExecHandle()
        verify(jmriTurnout).isNormal
        verify(keyValue).putValue("T/jmriName", "R", true)
        verify(eventLogger).logAsync(EventLogger.Type.Turnout, "T/jmriName", "reverse")
        assertThat(turnout.active).isFalse()
    }
}
