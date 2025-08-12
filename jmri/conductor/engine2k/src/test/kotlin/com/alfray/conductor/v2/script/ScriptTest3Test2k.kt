/*
 * Project: Conductor
 * Copyright (C) 2025 alf.labs gmail com,
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

package com.alfray.conductor.v2.script

import com.alflabs.conductor.dagger.FakeDazzSender
import com.alflabs.conductor.dagger.FakeEventLogger
import com.alflabs.conductor.dagger.FakeJsonSender
import com.alflabs.kv.IKeyValue
import com.alflabs.utils.FakeClock
import com.alfray.conductor.v2.script.impl.Block
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

/** Tests using script_test3.conductor.kts file. */
class ScriptTest3Test2k : ScriptTest2kBase() {
    @Inject lateinit var clockMillis: FakeClock
    @Inject lateinit var keyValue: IKeyValue
    @Inject lateinit var eventLogger: FakeEventLogger
    @Inject lateinit var jsonSender: FakeJsonSender
    @Inject lateinit var dazzSender: FakeDazzSender

    @Before
    fun setUp() {
        createComponent()
        scriptComponent.inject(this)
    }

    @Test
    fun testScript3() {
        loadScriptFromFile("script_test3")
        assertResultNoError()

        val mlToggle = conductorImpl.sensors["NS829"]!!
        val b311 = conductorImpl.blocks["NS769"]!! as Block
        val b321 = conductorImpl.blocks["NS771"]!! as Block

        assertThat(jsonSender.eventsGetAndClear()).isEmpty()
        assertThat(dazzSender.eventsGetAndClear().map {
            // remove any path prefixing the kts filename to keep unit test path-agnostic
            it.replace("\"[^\"]+[/\\\\]([^/\\\\]+.kts)".toRegex(), "\"$1")
        }).containsExactly(
            """
                {"key":"conductor/script","ts":"1970-01-01T00:00:01Z","st":true,"d":"{\"script\": \"script_test3.conductor.kts\"}"}
            """.trimIndent())

        assertThat(eventLogger.eventLogGetAndClear()).containsExactly(
            "<clock 1000> - S - S/NS769 B311 - OFF",
            "<clock 1000> - S - S/NS771 B321 - OFF",
            "<clock 1000> - R - Idle Mainline #0 ML Ready - ACTIVATED"
        ).inOrder()

        // Engine starts on B311
        b311.internalActive(true)
        b321.internalActive(false)

        // Simulate about 1 second.
        repeat(10) {
            clockMillis.add(100)
            execEngine.onExecHandle()
        }
        assertThat(eventLogger.eventLogGetAndClear()).containsExactly(
            "<clock 1100> - S - S/NS769 B311 - ON",
            "<clock 1100> - D - 1072 - Light ON",
            "<clock 1100> - D - 1072 - Bell OFF",
            "<clock 1100> - D - 1072 - F1 OFF",
            "<clock 1100> - R - Idle Mainline #0 ML Ready - ACTIVE",
        ).inOrder()

        assertThat(jsonSender.eventsGetAndClear()).containsExactly(
            """
                <clock 1100> - {
                  "toggle" : {
                    "passenger" : {
                      "ts" : "1970-01-01T00:00:01Z",
                      "value" : "Off"
                    }
                  }
                }
            """.trimIndent(),
        ).inOrder()
        assertThat(dazzSender.eventsGetAndClear()).containsExactly(
            """
                {"key":"toggle/passenger","ts":"1970-01-01T00:00:01Z","st":false,"d":""}
            """.trimIndent())

        // Simulate an activation but turning the toggle on-off for 100ms
        mlToggle.active(true)
        execEngine.onExecHandle()
        assertThat(eventLogger.eventLogGetAndClear()).containsExactly(
            "<clock 2000> - S - S/NS829 ML-Toggle - ON",
            "<clock 2000> - R - Idle Mainline #0 ML Ready - IDLE",
            "<clock 2000> - R - Sequence Mainline #2 Freight (1072) - ACTIVATED",
        ).inOrder()

        assertThat(jsonSender.eventsGetAndClear()).containsExactly(
            """
                <clock 2000> - {
                  "toggle" : {
                    "passenger" : {
                      "ts" : "1970-01-01T00:00:02Z",
                      "value" : "On"
                    }
                  }
                }
            """.trimIndent(),
        ).inOrder()
        assertThat(dazzSender.eventsGetAndClear()).containsExactly(
            """
                {"key":"toggle/passenger","ts":"1970-01-01T00:00:02Z","st":true,"d":""}
            """.trimIndent(),
            """
                {"key":"route/Freight_FR","ts":"1970-01-01T00:00:02Z","st":true,"d":"{\"name\":\"Freight\",\"th\":\"FR\",\"act\":1,\"err\":false,\"run\":\"Started\",\"sts\":\"1970-01-01T00:00:02Z\",\"nodes\":[]}"}
            """.trimIndent())

        clockMillis.add(100)
        mlToggle.active(false)

        // The route takes about 5 minutes, aka 300 seconds, at 0.1sec intervals
        // First the train leaves from the start block...
        repeat(1*60*10) {
            clockMillis.add(100)
            execEngine.onExecHandle()
        }
        assertThat(eventLogger.eventLogGetAndClear()).containsExactly(
            "<clock 2233> - S - S/NS829 ML-Toggle - OFF",
            "<clock 2233> - B - S/NS769 B311 - Now OCCUPIED",
            "<clock 2233> - R - Sequence Mainline #2 Freight (1072) - ACTIVE",
            "<clock 2333> - D - 1072 - Light ON",
            "<clock 2333> - D - 1072 - Sound ON",
            "<clock 2333> - D - 1072 - F8 OFF",
            "<clock 2433> - T - @timer@2 - start:2",
            "<clock 4433> - T - @timer@2 - activated",
            "<clock 4433> - T - @timer@10 - start:10",
            "<clock 4433> - D - 1072 - Horn",
            "<clock 4433> - D - 1072 - Light ON",
            "<clock 4433> - D - 1072 - Bell OFF",
            "<clock 4433> - D - 1072 - F1 OFF",
            "<clock 4433> - D - 1072 - 2",
            "<clock 14433> - T - @timer@10 - activated",
            "<clock 14433> - D - 1072 - 8",
            "<clock 14433> - D - 1072 - Horn",
        ).inOrder()

        assertThat(jsonSender.eventsGetAndClear()).containsExactly(
            """
                <clock 2233> - {
                  "toggle" : {
                    "passenger" : {
                      "ts" : "1970-01-01T00:00:02Z",
                      "value" : "Off"
                    }
                  }
                }
            """.trimIndent(),
        ).inOrder()
        assertThat(dazzSender.eventsGetAndClear()).containsExactly(
            """
                {"key":"toggle/passenger","ts":"1970-01-01T00:00:02Z","st":false,"d":""}
            """.trimIndent())

        // Simulate train progress by changing blocks B311->B321
        b321.internalActive(true)
        clockMillis.add(100)
        execEngine.onExecHandle()
        b311.internalActive(false)
        clockMillis.add(100)
        execEngine.onExecHandle()

        assertThat(eventLogger.eventLogGetAndClear()).containsExactly(
            "<clock 62233> - S - S/NS771 B321 - ON",
            "<clock 62233> - B - S/NS769 B311 - Was OCCUPIED for 60.00 seconds; Now TRAILING",
            "<clock 62233> - B - S/NS771 B321 - Now OCCUPIED",
            "<clock 62333> - S - S/NS769 B311 - OFF",
            "<clock 62333> - T - @timer@35 - start:35",
        ).inOrder()

        // Train stops at the station before reversing back
        repeat(2*60*10) {
            clockMillis.add(100)
            execEngine.onExecHandle()
        }
        assertThat(eventLogger.eventLogGetAndClear()).containsExactly(
            "<clock 97333> - T - @timer@35 - activated",
            "<clock 97333> - T - @timer@14 - start:14",
            "<clock 97333> - D - 1072 - Horn",
            "<clock 97333> - D - 1072 - Bell ON",
            "<clock 97333> - D - 1072 - F1 ON",
            "<clock 97333> - D - 1072 - 2",
            "<clock 111333> - T - @timer@14 - activated",
            "<clock 111333> - T - @timer@1 - start:1",
            "<clock 111333> - D - 1072 - Horn",
            "<clock 111333> - D - 1072 - 0",
            "<clock 111333> - D - 1072 - Bell OFF",
            "<clock 111333> - D - 1072 - F1 OFF",
            "<clock 112333> - T - @timer@1 - activated",
            "<clock 112333> - T - @timer@1 - start:1",
            "<clock 113333> - T - @timer@1 - activated",
            "<clock 113333> - T - @timer@28 - start:28",
            "<clock 141333> - T - @timer@28 - activated",
            "<clock 141333> - T - @timer@2 - start:2",
            "<clock 141333> - D - 1072 - Horn",
            "<clock 141333> - D - 1072 - -4",
            "<clock 141333> - D - 1072 - Bell ON",
            "<clock 141333> - D - 1072 - F1 ON",
            "<clock 143333> - T - @timer@2 - activated",
            "<clock 143333> - T - @timer@2 - start:2",
            "<clock 143333> - D - 1072 - Horn",
            "<clock 145333> - T - @timer@2 - activated",
            "<clock 145333> - D - 1072 - Horn",
            "<clock 145333> - D - 1072 - Bell OFF",
            "<clock 145333> - D - 1072 - F1 OFF",
        ).inOrder()

        // Simulate train progress by changing blocks B321->B311
        b311.internalActive(true)
        clockMillis.add(100)
        execEngine.onExecHandle()
        b321.internalActive(false)
        clockMillis.add(100)
        execEngine.onExecHandle()

        assertThat(eventLogger.eventLogGetAndClear()).containsExactly(
            "<clock 182433> - S - S/NS769 B311 - ON",
            "<clock 182433> - B - S/NS769 B311 - Was TRAILING for 120.20 seconds; Now EMPTY",
            "<clock 182433> - B - S/NS771 B321 - Was OCCUPIED for 120.20 seconds; Now TRAILING",
            "<clock 182433> - B - S/NS769 B311 - Was EMPTY for 0.00 seconds; Now OCCUPIED",
            "<clock 182533> - S - S/NS771 B321 - OFF",
            "<clock 182533> - T - @timer@24 - start:24",
        ).inOrder()

        // Train arrives back at start point and stops
        repeat(1*60*10) {
            clockMillis.add(100)
            execEngine.onExecHandle()
        }

        val expectedRouteJson = """ {
            "name": "Freight",
            "th": "FR",
            "act": 1,
            "err": false,
            "nodes": [ { 
                    "n": "B311.1",
                    "ms": 60000
                }, {
                    "n": "B321",
                    "ms": 120200
                }, {
                    "n": "B311.2",
                    "ms": 52100
                }
            ]}
        """.replace("\\s".toRegex(), "")

        val expectedRouteDazz = """ {
            "name": "Freight",
            "th": "FR",
            "act": 1,
            "err": false,
            "run": "Ended",
            "sts": "1970-01-01T00:00:02Z",
            "ets": "1970-01-01T00:03:54Z",
            "nodes": [ {
                    "n": "B311.1",
                    "ms": 60000
                }, {
                    "n": "B321",
                    "ms": 120200
                }, {
                    "n": "B311.2",
                    "ms": 52100
                }
            ]}
        """.replace("\\s".toRegex(), "")

        assertThat(eventLogger.eventLogGetAndClear()).containsExactly(
            "<clock 206533> - T - @timer@24 - activated",
            "<clock 206533> - T - @timer@6 - start:6",
            "<clock 206533> - D - 1072 - Bell ON",
            "<clock 206533> - D - 1072 - F1 ON",
            "<clock 212533> - T - @timer@6 - activated",
            "<clock 212533> - T - @timer@20 - start:20",
            "<clock 212533> - D - 1072 - Bell OFF",
            "<clock 212533> - D - 1072 - F1 OFF",
            "<clock 212533> - D - 1072 - Horn",
            "<clock 212533> - D - 1072 - 0",
            "<clock 232533> - T - @timer@20 - activated",
            "<clock 232533> - T - @timer@2 - start:2",
            "<clock 232533> - D - 1072 - Horn",
            "<clock 232533> - D - 1072 - Bell OFF",
            "<clock 232533> - D - 1072 - F1 OFF",
            "<clock 232533> - D - 1072 - Light OFF",
            "<clock 234533> - T - @timer@2 - activated",
            "<clock 234533> - D - 1072 - Sound OFF",
            "<clock 234533> - D - 1072 - F8 ON",
            "<clock 234533> - R - Sequence Mainline #2 Freight (1072) - IDLE",
            "<clock 234533> - R - Sequence Mainline #2 Freight (1072) - $expectedRouteDazz",
            "<clock 234533> - B - S/NS771 B321 - Was TRAILING for 52.10 seconds; Now EMPTY",
            "<clock 234533> - R - Idle Mainline #1 ML Wait - ACTIVATED",
            "<clock 234633> - R - Idle Mainline #1 ML Wait - ACTIVE",
            "<clock 234733> - T - @timer@60 - start:60",
        ).inOrder()

        // Train has a 60-second wait pause before being ready for the next activation
        repeat(1*60*10) {
            clockMillis.add(100)
            execEngine.onExecHandle()
        }
        assertThat(eventLogger.eventLogGetAndClear()).containsExactly(
            "<clock 294733> - T - @timer@60 - activated",
            "<clock 294733> - R - Idle Mainline #1 ML Wait - IDLE",
            "<clock 294733> - R - Idle Mainline #0 ML Ready - ACTIVATED",
            "<clock 294833> - D - 1072 - Light ON",
            "<clock 294833> - D - 1072 - Bell OFF",
            "<clock 294833> - D - 1072 - F1 OFF",
            "<clock 294833> - R - Idle Mainline #0 ML Ready - ACTIVE",
        ).inOrder()

        assertThat(jsonSender.eventsGetAndClear()).containsExactly(
            """
                <clock 234533> - {
                  "route_stats" : {
                    "freight_fr" : {
                      "ts" : "1970-01-01T00:03:54Z",
                      "value" : "${expectedRouteJson.replace("\"", "\\\"")}"
                    }
                  },
                  "toggle" : {
                    "passenger" : {
                      "ts" : "1970-01-01T00:00:02Z",
                      "value" : "Off"
                    }
                  }
                }
            """.trimIndent(),
        ).inOrder()
        assertThat(dazzSender.eventsGetAndClear()).containsExactly(
            """
                {"key":"route/Freight_FR","ts":"1970-01-01T00:00:02Z","st":true,"d":"${expectedRouteDazz.replace("\"", "\\\"")}"}
            """.trimIndent())
    }
}
