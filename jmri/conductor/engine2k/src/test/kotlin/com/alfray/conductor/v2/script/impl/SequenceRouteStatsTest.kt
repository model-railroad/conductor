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
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import dagger.internal.InstanceFactory
import org.junit.Before
import org.junit.Test
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.TimeZone

/** Tests for [SequenceRouteStats]. */
class SequenceRouteStatsTest {
    private lateinit var blockFactory: Block_Factory
    private lateinit var stat: SequenceRouteStats
    private val jmriProvider = FakeJmriProvider()
    private val eventLogger = mock<EventLogger>()
    private val keyValue = mock<IKeyValue>()
    private val condCache = CondCache()
    private val clock = FakeClock(1000)

    @Before
    fun setUp() {
        // Format timestamps using ISO 8601, forcing a UTC (ZULU) timezone.
        val df: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        df.timeZone = TimeZone.getTimeZone("UTC")

        // resource under test
        stat = SequenceRouteStats(clock, df, "MyRoute", "TH")

        blockFactory = Block_Factory(
            InstanceFactory.create(keyValue),
            InstanceFactory.create(jmriProvider),
            InstanceFactory.create(clock),
            InstanceFactory.create(condCache),
            InstanceFactory.create(eventLogger))
    }

    @Test
    fun toJsonString_Empty() {
        assertThat(stat.toJsonString()).isEqualTo("""
            |{"name":"MyRoute",
            |"th":"TH",
            |"act":0,
            |"err":false,
            |"nodes":[]}
        """.trimMargin().replace("\n",""))
    }

    @Test
    fun toJsonString_Error() {
        stat.activateAndReset()
        stat.setError()
        assertThat(stat.toJsonString()).isEqualTo("""
            |{"name":"MyRoute",
            |"th":"TH",
            |"act":1,
            |"err":true,
            |"nodes":[]}
        """.trimMargin().replace("\n",""))
    }

    @Test
    fun toJsonString_UniqueNodeNames() {
        stat.activateAndReset()
        stat.addNodeWithDurationMs(node("B1"), durationMs = 41, minSeconds = 10, maxSeconds = 110)
        stat.addNodeWithDurationMs(node("B2"), durationMs = 42, minSeconds = 20, maxSeconds = 120)
        stat.addNodeWithDurationMs(node("B3"), durationMs = 43, minSeconds = 30, maxSeconds = 130)
        stat.addNodeWithDurationMs(node("B4"), durationMs = 44, minSeconds = 40, maxSeconds = 140)

        assertThat(stat.toJsonString()).isEqualTo("""
            |{"name":"MyRoute",
            |"th":"TH",
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
        stat.activateAndReset()
        stat.addNodeWithDurationMs(node("B1"), durationMs = 41, minSeconds = 10, maxSeconds = 110)

        stat.activateAndReset()
        stat.addNodeWithDurationMs(node("B1"), durationMs = 41, minSeconds = 10, maxSeconds = 110)
        stat.addNodeWithDurationMs(node("B2"), durationMs = 42, minSeconds = 20, maxSeconds = 120)
        stat.addNodeWithDurationMs(node("B2"), durationMs = 43, minSeconds = 30, maxSeconds = 130)
        stat.addNodeWithDurationMs(node("B3"), durationMs = 44, minSeconds = 40, maxSeconds = 140)
        stat.addNodeWithDurationMs(node("B3"), durationMs = 45, minSeconds = 50, maxSeconds = 150)
        stat.addNodeWithDurationMs(node("B3"), durationMs = 46, minSeconds = 60, maxSeconds = 160)
        stat.addNodeWithDurationMs(node("B4"), durationMs = 47, minSeconds = 70, maxSeconds = 170)
        stat.addNodeWithDurationMs(node("B4"), durationMs = 48, minSeconds = 80, maxSeconds = 180)
        stat.addNodeWithDurationMs(node("B4"), durationMs = 49, minSeconds = 90, maxSeconds = 190)
        stat.addNodeWithDurationMs(node("B4"), durationMs = 50, minSeconds = 99, maxSeconds = 199)

        assertThat(stat.toJsonString()).isEqualTo("""
            |{"name":"MyRoute",
            |"th":"TH",
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
        stat.activateAndReset()
        stat.addNodeWithDurationMs(node("B1"), durationMs = 41, minSeconds = 10, maxSeconds = 110)

        stat.activateAndReset()
        stat.activateAndReset()
        stat.addNodeWithDurationMs(node("B1"), durationMs = 41, minSeconds = 10, maxSeconds = 110)
        stat.addNodeWithDurationMs(node("B2"), durationMs = 42, minSeconds = 20, maxSeconds = 120)
        stat.addNodeWithDurationMs(node("B3"), durationMs = 43, minSeconds = 30, maxSeconds = 130)
        stat.addNodeWithDurationMs(node("B4"), durationMs = 44, minSeconds = 40, maxSeconds = 140)
        stat.addNodeWithDurationMs(node("B3"), durationMs = 45, minSeconds = 50, maxSeconds = 150)
        stat.addNodeWithDurationMs(node("B2"), durationMs = 46, minSeconds = 60, maxSeconds = 160)
        stat.addNodeWithDurationMs(node("B1"), durationMs = 47, minSeconds = 70, maxSeconds = 170)

        assertThat(stat.toJsonString()).isEqualTo("""
            |{"name":"MyRoute",
            |"th":"TH",
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

    @Test
    fun toDazzString_Empty() {
        assertThat(stat.toDazzString()).isEqualTo("""
            |{"name":"MyRoute",
            |"th":"TH",
            |"act":0,
            |"err":false,
            |"run":"Unknown",
            |"sts":"1970-01-01T00:00:00Z",
            |"nodes":[]}
        """.trimMargin().replace("\n",""))
    }

    @Test
    fun toDazzString_Error() {
        stat.activateAndReset()
        stat.setError()
        clock.add(10_000)
        stat.setRunning(SequenceRouteStats.Running.Ended)
        assertThat(stat.toDazzString()).isEqualTo("""
            |{"name":"MyRoute",
            |"th":"TH",
            |"act":1,
            |"err":true,
            |"run":"Ended",
            |"sts":"1970-01-01T00:00:01Z",
            |"ets":"1970-01-01T00:00:11Z",
            |"nodes":[]}
        """.trimMargin().replace("\n",""))
    }

    @Test
    fun toDazzString_UniqueNodeNames() {
        stat.activateAndReset()
        stat.addNodeWithDurationMs(node("B1"), durationMs = 41, minSeconds = 10, maxSeconds = 110)
        stat.addNodeWithDurationMs(node("B2"), durationMs = 42, minSeconds = 20, maxSeconds = 120)
        stat.addNodeWithDurationMs(node("B3"), durationMs = 43, minSeconds = 30, maxSeconds = 130)
        stat.addNodeWithDurationMs(node("B4"), durationMs = 44, minSeconds = 40, maxSeconds = 140)
        clock.add(10_000)
        stat.setRunning(SequenceRouteStats.Running.Ended)

        assertThat(stat.toDazzString()).isEqualTo("""
            |{"name":"MyRoute",
            |"th":"TH",
            |"act":1,
            |"err":false,
            |"run":"Ended",
            |"sts":"1970-01-01T00:00:01Z",
            |"ets":"1970-01-01T00:00:11Z",
            |"nodes":[
              |{"n":"B1","ms":41,"mis":10,"mas":110},
              |{"n":"B2","ms":42,"mis":20,"mas":120},
              |{"n":"B3","ms":43,"mis":30,"mas":130},
              |{"n":"B4","ms":44,"mis":40,"mas":140}]}
        """.trimMargin().replace("\n",""))
    }

    @Test
    fun toDazzString_RepeatedNodeNames() {
        stat.activateAndReset()
        stat.addNodeWithDurationMs(node("B1"), durationMs = 41, minSeconds = 10, maxSeconds = 110)

        stat.activateAndReset()
        stat.addNodeWithDurationMs(node("B1"), durationMs = 41, minSeconds = 10, maxSeconds = 110)
        stat.addNodeWithDurationMs(node("B2"), durationMs = 42, minSeconds = 20, maxSeconds = 120)
        stat.addNodeWithDurationMs(node("B2"), durationMs = 43, minSeconds = 30, maxSeconds = 130)
        stat.addNodeWithDurationMs(node("B3"), durationMs = 44, minSeconds = 40, maxSeconds = 140)
        stat.addNodeWithDurationMs(node("B3"), durationMs = 45, minSeconds = 50, maxSeconds = 150)
        stat.addNodeWithDurationMs(node("B3"), durationMs = 46, minSeconds = 60, maxSeconds = 160)
        stat.addNodeWithDurationMs(node("B4"), durationMs = 47, minSeconds = 70, maxSeconds = 170)
        stat.addNodeWithDurationMs(node("B4"), durationMs = 48, minSeconds = 80, maxSeconds = 180)
        stat.addNodeWithDurationMs(node("B4"), durationMs = 49, minSeconds = 90, maxSeconds = 190)
        stat.addNodeWithDurationMs(node("B4"), durationMs = 50, minSeconds = 99, maxSeconds = 199)
        clock.add(10_000)
        stat.setRunning(SequenceRouteStats.Running.Ended)

        assertThat(stat.toDazzString()).isEqualTo("""
            |{"name":"MyRoute",
            |"th":"TH",
            |"act":2,
            |"err":false,
            |"run":"Ended",
            |"sts":"1970-01-01T00:00:01Z",
            |"ets":"1970-01-01T00:00:11Z",
            |"nodes":[
              |{"n":"B1","ms":41,"mis":10,"mas":110},
              |{"n":"B2.1","ms":42,"mis":20,"mas":120},
              |{"n":"B2.2","ms":43,"mis":30,"mas":130},
              |{"n":"B3.1","ms":44,"mis":40,"mas":140},
              |{"n":"B3.2","ms":45,"mis":50,"mas":150},
              |{"n":"B3.3","ms":46,"mis":60,"mas":160},
              |{"n":"B4.1","ms":47,"mis":70,"mas":170},
              |{"n":"B4.2","ms":48,"mis":80,"mas":180},
              |{"n":"B4.3","ms":49,"mis":90,"mas":190},
              |{"n":"B4.4","ms":50,"mis":99,"mas":199}]}
        """.trimMargin().replace("\n",""))
    }

    @Test
    fun toDazzString_ShuttleNodeNames() {
        stat.activateAndReset()
        stat.addNodeWithDurationMs(node("B1"), durationMs = 41, minSeconds = 10, maxSeconds = 110)

        stat.activateAndReset()
        stat.activateAndReset()
        stat.addNodeWithDurationMs(node("B1"), durationMs = 41, minSeconds = 10, maxSeconds = 110)
        stat.addNodeWithDurationMs(node("B2"), durationMs = 42, minSeconds = 20, maxSeconds = 120)
        stat.addNodeWithDurationMs(node("B3"), durationMs = 43, minSeconds = 30, maxSeconds = 130)
        stat.addNodeWithDurationMs(node("B4"), durationMs = 44, minSeconds = 40, maxSeconds = 140)
        stat.addNodeWithDurationMs(node("B3"), durationMs = 45, minSeconds = 50, maxSeconds = 150)
        stat.addNodeWithDurationMs(node("B2"), durationMs = 46, minSeconds = 60, maxSeconds = 160)
        stat.addNodeWithDurationMs(node("B1"), durationMs = 47, minSeconds = 70, maxSeconds = 170)
        clock.add(10_000)
        stat.setRunning(SequenceRouteStats.Running.Ended)

        assertThat(stat.toDazzString()).isEqualTo("""
            |{"name":"MyRoute",
            |"th":"TH",
            |"act":3,
            |"err":false,
            |"run":"Ended",
            |"sts":"1970-01-01T00:00:01Z",
            |"ets":"1970-01-01T00:00:11Z",
            |"nodes":[
              |{"n":"B1.1","ms":41,"mis":10,"mas":110},
              |{"n":"B2.1","ms":42,"mis":20,"mas":120},
              |{"n":"B3.1","ms":43,"mis":30,"mas":130},
              |{"n":"B4","ms":44,"mis":40,"mas":140},
              |{"n":"B3.2","ms":45,"mis":50,"mas":150},
              |{"n":"B2.2","ms":46,"mis":60,"mas":160},
              |{"n":"B1.2","ms":47,"mis":70,"mas":170}]}
        """.trimMargin().replace("\n",""))
    }

    private fun node(name: String) =
        NodeBuilder(jmriProvider, blockFactory.get(name)).create()
}
