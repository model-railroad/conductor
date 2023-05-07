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
import com.alflabs.conductor.jmri.IJmriSensor
import com.alflabs.conductor.util.EventLogger
import com.alflabs.kv.IKeyValue
import com.alfray.conductor.v2.script.CondCache
import com.alfray.conductor.v2.script.dsl.IBlock
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.verify
import dagger.internal.InstanceFactory
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.never

class BlockTest {
    private val jmriSensor = object : IJmriSensor {
        var _active = false
        override fun isActive(): Boolean  = _active
        override fun setActive(active: Boolean) { _active = active }
    }
    private val jmriProvider = mock<IJmriProvider> { on { getSensor("jmriName") } doReturn jmriSensor }
    private val eventLogger = mock<EventLogger>()
    private val keyValue = mock<IKeyValue>()
    private val condCache = CondCache()
    private lateinit var block: Block

    @Before
    fun setUp() {
        val factory = Block_Factory(
            InstanceFactory.create(keyValue),
            InstanceFactory.create(condCache),
            InstanceFactory.create(eventLogger),
            InstanceFactory.create(jmriProvider))
        block = factory.get("jmriName")

        block.onExecStart()
        verify(jmriProvider).getSensor("jmriName")
        verify(keyValue).putValue("S/jmriName", "OFF", true)
        reset(keyValue)
        reset(eventLogger)
    }

    @Test
    fun testIsActive() {
        block.named("Block-Name")
        jmriSensor.isActive = true
        // the JMRI state is not reflected in the internal state until after onExecHandle
        assertThat(block.active).isFalse()
        verify(keyValue, never()).putValue(anyString(), anyString(), anyBoolean())

        block.onExecHandle()
        assertThat(block.active).isTrue()
        verify(keyValue).putValue("S/jmriName", "ON", true)
        verify(eventLogger).logAsync(EventLogger.Type.Sensor, "S/jmriName", "Block-Name ON")
        reset(keyValue)

        jmriSensor.isActive = false
        assertThat(block.active).isTrue()
        verify(keyValue, never()).putValue(anyString(), anyString(), anyBoolean())
        block.onExecHandle()
        assertThat(block.active).isFalse()
        verify(keyValue).putValue("S/jmriName", "OFF", true)
        verify(eventLogger).logAsync(EventLogger.Type.Sensor, "S/jmriName", "Block-Name OFF")
    }

    @Test
    fun testChangeState() {
        block.named("Block-Name")

        // This first "changeState" doesn't change anything since the block stats EMPTY.
        // Consequently, it does not generate a log event since no state actually changes.
        block.changeState(IBlock.State.EMPTY)
        verify(eventLogger, never()).logAsync(EventLogger.Type.Block, "S/jmriName", "Block-Name ON")

        block.changeState(IBlock.State.OCCUPIED)
        verify(eventLogger).logAsync(EventLogger.Type.Block, "S/jmriName", "Block-Name OCCUPIED")

        block.changeState(IBlock.State.TRAILING)
        verify(eventLogger).logAsync(EventLogger.Type.Block, "S/jmriName", "Block-Name TRAILING")

        block.changeState(IBlock.State.EMPTY)
        verify(eventLogger).logAsync(EventLogger.Type.Block, "S/jmriName", "Block-Name EMPTY")
    }

    @Test
    fun testName() {
        assertThat(block.systemName).isEqualTo("jmriName")
        assertThat(block.name).isEqualTo("jmriName")
        assertThat(block.toString()).isEqualTo("{jmriName}")

        block.internalActive(true)
        block.onExecHandle()
        assertThat(block.toString()).isEqualTo("<jmriName>")

        block.internalActive(false)
        block.onExecHandle()
        block.named("Block-Name")
        assertThat(block.systemName).isEqualTo("jmriName")
        assertThat(block.name).isEqualTo("Block-Name")
        assertThat(block.toString()).isEqualTo("{Block-Name [jmriName]}")

        block.internalActive(true)
        block.onExecHandle()
        assertThat(block.toString()).isEqualTo("<Block-Name [jmriName]>")
    }
}
