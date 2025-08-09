/*
 * Project: DazzServ
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

package com.alfray.dazzserv.store

import com.alflabs.dazzserv.store.DataEntry
import com.alflabs.utils.FakeFileOps
import com.alfray.dazzserv.dagger.DaggerIMainTestComponent
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.charset.StandardCharsets
import javax.inject.Inject


class DazzStoreTest {
    @Inject lateinit var fileOps: FakeFileOps
    @Inject lateinit var ds: DataStore

    @Before
    fun setUp() {
        val component = DaggerIMainTestComponent.factory().createComponent()
        component.inject(this)
    }

    @Test
    fun testEntryToJson() {
        val entry = DataEntry("toggles/entry1", "1970-01-01T00:03:54Z", true, "( some payload )")
        val json = ds.entryToJson(entry)

        assertThat(json).isEqualTo(
            """
                {"key":"toggles/entry1","ts":"1970-01-01T00:03:54Z","st":true,"d":"( some payload )"}
            """.trimIndent())
    }

    @Test
    fun testStoreToJson() {
        ds.add(DataEntry("toggles/entry1", "1970-01-04T00:06:59Z", true, "payload 1"))
        ds.add(DataEntry("toggles/entry2", "1970-01-03T00:05:48Z", true, "payload 2"))
        ds.add(DataEntry("toggles/entry1", "1970-01-01T00:04:37Z", true, "payload 3"))
        ds.add(DataEntry("toggles/entry2", "1970-01-02T00:03:26Z", true, "payload 4"))

        val json = ds.storeToJson()

        assertThat(json).isEqualTo(
            """
                {
                  "toggles/entry1": {
                    "entries": {
                      "1970-01-04T00:06:59Z": {"key": "toggles/entry1", "ts": "1970-01-04T00:06:59Z", "st": true, "d": "payload 1"}, 
                      "1970-01-01T00:04:37Z": {"key": "toggles/entry1", "ts": "1970-01-01T00:04:37Z", "st": true, "d": "payload 3"}
                    }
                  }, 
                  "toggles/entry2": {
                    "entries": {
                      "1970-01-03T00:05:48Z": {"key": "toggles/entry2", "ts": "1970-01-03T00:05:48Z", "st": true, "d": "payload 2"}, 
                      "1970-01-02T00:03:26Z": {"key": "toggles/entry2", "ts": "1970-01-02T00:03:26Z", "st": true, "d": "payload 4"}
                    }
                  }
                }
            """.trimIndent())
    }

    @Test
    fun testStore() {
        var inputJson = """
                {
                    "st":true,
                    "ts":"1970-01-01T00:03:54Z",
                    "key":"toggles/entry1",
                    "d":"( some payload )"}
            """.trimIndent()

        assertThat(ds.store(inputJson)).isTrue()

        var storeJson = ds.storeToJson()

        assertThat(storeJson).isEqualTo(
            """
                {
                  "toggles/entry1": {
                    "entries": {
                      "1970-01-01T00:03:54Z": {"key": "toggles/entry1", "ts": "1970-01-01T00:03:54Z", "st": true, "d": "( some payload )"}
                    }
                  }
                }
            """.trimIndent())

        val file = File("save.dat")
        ds.saveTo(file)

        assertThat(fileOps.isFile(file)).isTrue()
        assertThat(fileOps.toString(file, StandardCharsets.UTF_8)).isEqualTo(
            """
                {"key":"toggles/entry1","ts":"1970-01-01T00:03:54Z","st":true,"d":"( some payload )"}

            """.trimIndent()
        )

        // State and Payload data are optional
        inputJson = """
                {"key":"toggles/entry1", "ts":"1970-01-02T03:04:56Z" }
            """.trimIndent()

        assertThat(ds.store(inputJson)).isTrue()

        storeJson = ds.storeToJson()

        assertThat(storeJson).isEqualTo(
            """
                {
                  "toggles/entry1": {
                    "entries": {
                      "1970-01-02T03:04:56Z": {"key": "toggles/entry1", "ts": "1970-01-02T03:04:56Z", "st": false, "d": ""}, 
                      "1970-01-01T00:03:54Z": {"key": "toggles/entry1", "ts": "1970-01-01T00:03:54Z", "st": true, "d": "( some payload )"}
                    }
                  }
                }
            """.trimIndent())

        ds.saveTo(file)

        assertThat(fileOps.isFile(file)).isTrue()
        assertThat(fileOps.toString(file, StandardCharsets.UTF_8)).isEqualTo(
            """
                {"key":"toggles/entry1","ts":"1970-01-01T00:03:54Z","st":true,"d":"( some payload )"}
                {"key":"toggles/entry1","ts":"1970-01-02T03:04:56Z","st":false,"d":""}

            """.trimIndent()
        )
    }

    @Test
    fun testStoreRejected() {
        // Key is mandatory
        var inputJson = """
                {
                    "ts":"1970-01-01T00:03:54Z",
                    "st":true,
                    "d":"( some payload )"}
            """.trimIndent()

        assertThat(ds.store(inputJson)).isFalse()

        // Timestamp is mandatory
        inputJson = """
                {
                    "key":"toggles/entry1",
                    "st":true,
                    "d":"( some payload )"}
            """.trimIndent()

        assertThat(ds.store(inputJson)).isFalse()

        // There is no data in the store and nothing to save to the output file
        val storeJson = ds.storeToJson()

        assertThat(storeJson).isEqualTo("{ }")

        val file = File("save.dat")
        ds.saveTo(file)
        assertThat(fileOps.isFile(file)).isFalse()
    }

    @Test
    fun testQueryToJson() {
        ds.add(DataEntry("toggles/entry1", "1970-01-04T00:06:59Z", true, "payload 1"))
        ds.add(DataEntry("toggles/entry2", "1970-01-03T00:05:48Z", true, "payload 2"))
        ds.add(DataEntry("toggles/entry1", "1970-01-01T00:04:37Z", true, "payload 3"))
        ds.add(DataEntry("toggles/entry2", "1970-01-02T00:03:26Z", true, "payload 4"))

        assertThat(ds.queryToJson("blah")).isEmpty()
        assertThat(ds.queryToJson("toggles/")).isEmpty()

        assertThat(ds.queryToJson("toggles/entry1")).isEqualTo(
            """
                {
                  "toggles/entry1": {
                    "entries": {
                      "1970-01-04T00:06:59Z": {"key": "toggles/entry1", "ts": "1970-01-04T00:06:59Z", "st": true, "d": "payload 1"}, 
                      "1970-01-01T00:04:37Z": {"key": "toggles/entry1", "ts": "1970-01-01T00:04:37Z", "st": true, "d": "payload 3"}
                    }
                  }
                }
            """.trimIndent())


        assertThat(ds.queryToJson("*/entry2")).isEqualTo(
            """
                {
                  "toggles/entry2": {
                    "entries": {
                      "1970-01-03T00:05:48Z": {"key": "toggles/entry2", "ts": "1970-01-03T00:05:48Z", "st": true, "d": "payload 2"}, 
                      "1970-01-02T00:03:26Z": {"key": "toggles/entry2", "ts": "1970-01-02T00:03:26Z", "st": true, "d": "payload 4"}
                    }
                  }
                }
            """.trimIndent())

        assertThat(ds.queryToJson("to**")).isEqualTo(
            """
                {
                  "toggles/entry1": {
                    "entries": {
                      "1970-01-04T00:06:59Z": {"key": "toggles/entry1", "ts": "1970-01-04T00:06:59Z", "st": true, "d": "payload 1"}, 
                      "1970-01-01T00:04:37Z": {"key": "toggles/entry1", "ts": "1970-01-01T00:04:37Z", "st": true, "d": "payload 3"}
                    }
                  }, 
                  "toggles/entry2": {
                    "entries": {
                      "1970-01-03T00:05:48Z": {"key": "toggles/entry2", "ts": "1970-01-03T00:05:48Z", "st": true, "d": "payload 2"}, 
                      "1970-01-02T00:03:26Z": {"key": "toggles/entry2", "ts": "1970-01-02T00:03:26Z", "st": true, "d": "payload 4"}
                    }
                  }
                }
            """.trimIndent())
    }

    @Test
    fun testLiveV1ToJson() {
        ds.add(DataEntry("toggles/entry1", "1970-01-04T00:06:59Z", true, "payload 1"))
        ds.add(DataEntry("toggles/entry2", "1970-01-03T00:05:48Z", true, "payload 2"))
        ds.add(DataEntry("toggles/entry1", "1970-01-01T00:04:37Z", true, "payload 3"))
        ds.add(DataEntry("toggles/entry2", "1970-01-02T00:03:26Z", true, "payload 4"))

        assertThat(ds.liveToJson(1)).isEmpty()
    }

    @Test
    fun testLiveV2ToJson() {
        ds.add(DataEntry("toggles/entry1", "1970-01-04T00:06:59Z", true, "payload 1"))
        ds.add(DataEntry("toggles/entry2", "1970-01-03T00:05:48Z", true, "payload 2"))
        ds.add(DataEntry("toggles/entry1", "1970-01-01T00:04:37Z", true, "payload 3"))
        ds.add(DataEntry("toggles/entry2", "1970-01-02T00:03:26Z", true, "payload 4"))

        assertThat(ds.liveToJson(2)).isEmpty()
    }

    @Test
    fun testHistoryToJson() {
        ds.add(DataEntry("toggles/entry1", "1970-01-04T00:06:59Z", true, "payload 1"))
        ds.add(DataEntry("toggles/entry2", "1970-01-03T00:05:48Z", true, "payload 2"))
        ds.add(DataEntry("toggles/entry1", "1970-01-01T00:04:37Z", false, "payload 3"))
        ds.add(DataEntry("toggles/entry2", "1970-01-02T00:03:26Z", false, "payload 4"))
        ds.add(DataEntry("toggles/entry1", "1970-01-05T00:06:89Z", true, "payload 5"))
        ds.add(DataEntry("toggles/entry2", "1970-01-06T00:07:89Z", true, "payload 6"))

        assertThat(ds.historyToJson()).isEqualTo(
            """
                {
                  "toggles/entry1": {
                    "entries": {
                      "1970-01-05T00:06:89Z": {"key": "toggles/entry1", "ts": "1970-01-05T00:06:89Z", "st": true, "d": "payload 5"}, 
                      "1970-01-04T00:06:59Z": {"key": "toggles/entry1", "ts": "1970-01-04T00:06:59Z", "st": true, "d": "payload 1"}
                    }
                  }, 
                  "toggles/entry2": {
                    "entries": {
                      "1970-01-06T00:07:89Z": {"key": "toggles/entry2", "ts": "1970-01-06T00:07:89Z", "st": true, "d": "payload 6"}, 
                      "1970-01-03T00:05:48Z": {"key": "toggles/entry2", "ts": "1970-01-03T00:05:48Z", "st": true, "d": "payload 2"}
                    }
                  }
                }
            """.trimIndent())
    }

}
