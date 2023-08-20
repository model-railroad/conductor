/*
 * Project: Conductor
 * Copyright (C) 2023 alf.labs gmail com,
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

import com.alflabs.conductor.jmri.FakeJmriProvider
import com.alflabs.conductor.util.EventLogger
import com.alflabs.kv.IKeyValue
import com.alflabs.utils.FakeClock
import com.alfray.conductor.v2.script.CondCache
import org.junit.Before
import org.junit.Test

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import dagger.internal.InstanceFactory

/** Tests for [SequenceRouteStats]. */
class SequenceRouteStatsTest {
    private lateinit var blockFactory: Block_Factory
    private val jmriProvider = FakeJmriProvider()
    private val eventLogger = mock<EventLogger>()
    private val keyValue = mock<IKeyValue>()
    private val condCache = CondCache()
    private val clock = FakeClock(1000)

    @Before
    fun setUp() {
        blockFactory = Block_Factory(
            InstanceFactory.create(keyValue),
            InstanceFactory.create(jmriProvider),
            InstanceFactory.create(clock),
            InstanceFactory.create(condCache),
            InstanceFactory.create(eventLogger))
    }

    @Test
    fun toJsonString_Empty() {
        val stat = SequenceRouteStats("MyRoute")
        assertThat(stat.toJsonString()).isEqualTo("""
            |{"name":"MyRoute",
            |"act":0,
            |"err":false,
            |"nodes":[]}
        """.trimMargin().replace("\n",""))
    }

    @Test
    fun toJsonString_Error() {
        val stat = SequenceRouteStats("MyRoute")
        stat.activateAndReset()
        stat.setError()
        assertThat(stat.toJsonString()).isEqualTo("""
            |{"name":"MyRoute",
            |"act":1,
            |"err":true,
            |"nodes":[]}
        """.trimMargin().replace("\n",""))
    }

    @Test
    fun toJsonString_UniqueNodeNames() {
        val stat = SequenceRouteStats("MyRoute")
        stat.activateAndReset()
        stat.addNodeWithDurationMs(node("B1"), 41)
        stat.addNodeWithDurationMs(node("B2"), 42)
        stat.addNodeWithDurationMs(node("B3"), 43)
        stat.addNodeWithDurationMs(node("B4"), 44)

        assertThat(stat.toJsonString()).isEqualTo("""
            |{"name":"MyRoute",
            |"act":1,
            |"err":false,
            |"nodes":[
              |{"n":"B1","ms":41},
              |{"n":"B2","ms":42},
              |{"n":"B3","ms":43},
              |{"n":"B4","ms":44}]}
        """.trimMargin().replace("\n",""))
    }

    @Test
    fun toJsonString_RepeatedNodeNames() {
        val stat = SequenceRouteStats("MyRoute")
        stat.activateAndReset()
        stat.addNodeWithDurationMs(node("B1"), 41)

        stat.activateAndReset()
        stat.addNodeWithDurationMs(node("B1"), 41)
        stat.addNodeWithDurationMs(node("B2"), 42)
        stat.addNodeWithDurationMs(node("B2"), 43)
        stat.addNodeWithDurationMs(node("B3"), 44)
        stat.addNodeWithDurationMs(node("B3"), 45)
        stat.addNodeWithDurationMs(node("B3"), 46)
        stat.addNodeWithDurationMs(node("B4"), 47)
        stat.addNodeWithDurationMs(node("B4"), 48)
        stat.addNodeWithDurationMs(node("B4"), 49)
        stat.addNodeWithDurationMs(node("B4"), 50)

        assertThat(stat.toJsonString()).isEqualTo("""
            |{"name":"MyRoute",
            |"act":2,
            |"err":false,
            |"nodes":[
              |{"n":"B1","ms":41},
              |{"n":"B2.1","ms":42},
              |{"n":"B2.2","ms":43},
              |{"n":"B3.1","ms":44},
              |{"n":"B3.2","ms":45},
              |{"n":"B3.3","ms":46},
              |{"n":"B4.1","ms":47},
              |{"n":"B4.2","ms":48},
              |{"n":"B4.3","ms":49},
              |{"n":"B4.4","ms":50}]}
        """.trimMargin().replace("\n",""))
    }

    @Test
    fun toJsonString_ShuttleNodeNames() {
        val stat = SequenceRouteStats("MyRoute")
        stat.activateAndReset()
        stat.addNodeWithDurationMs(node("B1"), 41)

        stat.activateAndReset()
        stat.activateAndReset()
        stat.addNodeWithDurationMs(node("B1"), 41)
        stat.addNodeWithDurationMs(node("B2"), 42)
        stat.addNodeWithDurationMs(node("B3"), 43)
        stat.addNodeWithDurationMs(node("B4"), 44)
        stat.addNodeWithDurationMs(node("B3"), 45)
        stat.addNodeWithDurationMs(node("B2"), 46)
        stat.addNodeWithDurationMs(node("B1"), 47)

        assertThat(stat.toJsonString()).isEqualTo("""
            |{"name":"MyRoute",
            |"act":3,
            |"err":false,
            |"nodes":[
              |{"n":"B1.1","ms":41},
              |{"n":"B2.1","ms":42},
              |{"n":"B3.1","ms":43},
              |{"n":"B4","ms":44},
              |{"n":"B3.2","ms":45},
              |{"n":"B2.2","ms":46},
              |{"n":"B1.2","ms":47}]}
        """.trimMargin().replace("\n",""))
    }

    private fun node(name: String) =
        NodeBuilder(jmriProvider, blockFactory.get(name)).create()
}
