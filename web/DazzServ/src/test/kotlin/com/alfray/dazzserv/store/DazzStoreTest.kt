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
    fun testStoreUpdatedEvent() {
        ds.add(DataEntry("toggles/entry1", "1970-01-02T03:04:05Z", true, "payload 1"))

        assertThat(ds.storeToJson()).isEqualTo(
            """
                {
                  "toggles/entry1": {
                    "entries": {
                      "1970-01-02T03:04:05Z": {"key": "toggles/entry1", "ts": "1970-01-02T03:04:05Z", "st": true, "d": "payload 1"}
                    }
                  }
                }
            """.trimIndent())

        // Update the same event: same key/timestamp, new state/payload.
        ds.add(DataEntry("toggles/entry1", "1970-01-02T03:04:05Z", false, "payload 2"))

        assertThat(ds.storeToJson()).isEqualTo(
            """
                {
                  "toggles/entry1": {
                    "entries": {
                      "1970-01-02T03:04:05Z": {"key": "toggles/entry1", "ts": "1970-01-02T03:04:05Z", "st": false, "d": "payload 2"}
                    }
                  }
                }
            """.trimIndent())
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
    fun testLiveToJson() {
        ds.add(DataEntry("entryA", "ts0001", true , "run A.01 ok"))
        ds.add(DataEntry("entryA", "ts0002", true , "run A.02 ok"))
        ds.add(DataEntry("entryA", "ts0003", true , "run A.03 ok"))
        ds.add(DataEntry("entryA", "ts0004", true , "run A.04 ok"))

        ds.add(DataEntry("entryB", "ts0012", true , "run B.08 ok"))
        ds.add(DataEntry("entryB", "ts0013", true , "run B.09 ok"))
        ds.add(DataEntry("entryB", "ts0014", true , "run B.10 ok"))
        ds.add(DataEntry("entryB", "ts0015", true , "run B.11 ok"))
        ds.add(DataEntry("entryB", "ts0016", false, "run B.12 fail"))
        ds.add(DataEntry("entryB", "ts0017", false, "run B.13 fail"))
        ds.add(DataEntry("entryB", "ts0018", false, "run B.14 fail"))

        ds.add(DataEntry("entryC", "ts0001", true , "run C.01 ok"))
        ds.add(DataEntry("entryC", "ts0003", true , "run C.02 ok"))
        ds.add(DataEntry("entryC", "ts0004", false, "run C.02 fail"))
        ds.add(DataEntry("entryC", "ts0005", false, "run C.02 fail"))
        ds.add(DataEntry("entryC", "ts0007", true , "run C.04 ok"))
        ds.add(DataEntry("entryC", "ts0008", false, "run C.04 fail"))
        ds.add(DataEntry("entryC", "ts0009", false, "run C.04 fail"))

        assertThat(ds.liveToJson()).isEqualTo(
            """
                {
                  "entryA": {
                    "entries": {
                      "ts0004": {"key": "entryA", "ts": "ts0004", "st": true, "d": "run A.04 ok"}, 
                      "ts0003": {"key": "entryA", "ts": "ts0003", "st": true, "d": "run A.03 ok"}
                    }
                  }, 
                  "entryB": {
                    "entries": {
                      "ts0018": {"key": "entryB", "ts": "ts0018", "st": false, "d": "run B.14 fail"}, 
                      "ts0017": {"key": "entryB", "ts": "ts0017", "st": false, "d": "run B.13 fail"}, 
                      "ts0016": {"key": "entryB", "ts": "ts0016", "st": false, "d": "run B.12 fail"}, 
                      "ts0015": {"key": "entryB", "ts": "ts0015", "st": true, "d": "run B.11 ok"}, 
                      "ts0014": {"key": "entryB", "ts": "ts0014", "st": true, "d": "run B.10 ok"}
                    }
                  }, 
                  "entryC": {
                    "entries": {
                      "ts0009": {"key": "entryC", "ts": "ts0009", "st": false, "d": "run C.04 fail"}, 
                      "ts0008": {"key": "entryC", "ts": "ts0008", "st": false, "d": "run C.04 fail"}, 
                      "ts0007": {"key": "entryC", "ts": "ts0007", "st": true, "d": "run C.04 ok"}
                    }
                  }
                }
            """.trimIndent())
    }

    @Test
    fun testHistoryToJson() {
        ds.add(DataEntry("entryA", "ts0001", true , "run A.01 ok"))
        ds.add(DataEntry("entryA", "ts0002", false, "run A.01 fail"))
        ds.add(DataEntry("entryA", "ts0003", true , "run A.02 ok"))
        ds.add(DataEntry("entryA", "ts0004", false, "run A.02 fail"))
        ds.add(DataEntry("entryA", "ts0005", false, "run A.02 fail"))
        ds.add(DataEntry("entryA", "ts0006", true , "run A.03 ok"))
        ds.add(DataEntry("entryA", "ts0007", true , "run A.04 ok"))
        ds.add(DataEntry("entryA", "ts0008", true , "run A.05 ok"))
        ds.add(DataEntry("entryA", "ts0009", true , "run A.06 ok"))
        ds.add(DataEntry("entryA", "ts0010", true , "run A.07 ok"))
        ds.add(DataEntry("entryA", "ts0011", true , "run A.08 ok"))
        ds.add(DataEntry("entryA", "ts0012", true , "run A.09 ok"))
        ds.add(DataEntry("entryA", "ts0013", true , "run A.10 ok"))
        ds.add(DataEntry("entryA", "ts0014", true , "run A.11 ok"))

        ds.add(DataEntry("entryB", "ts0005", true , "run B.01 ok"))
        ds.add(DataEntry("entryB", "ts0006", true , "run B.02 ok"))
        ds.add(DataEntry("entryB", "ts0007", true , "run B.03 ok"))
        ds.add(DataEntry("entryB", "ts0008", true , "run B.04 ok"))
        ds.add(DataEntry("entryB", "ts0009", true , "run B.05 ok"))
        ds.add(DataEntry("entryB", "ts0010", true , "run B.06 ok"))
        ds.add(DataEntry("entryB", "ts0011", true , "run B.07 ok"))
        ds.add(DataEntry("entryB", "ts0012", true , "run B.08 ok"))
        ds.add(DataEntry("entryB", "ts0013", true , "run B.09 ok"))
        ds.add(DataEntry("entryB", "ts0014", true , "run B.10 ok"))
        ds.add(DataEntry("entryB", "ts0015", true , "run B.11 ok"))
        ds.add(DataEntry("entryB", "ts0016", false, "run B.12 fail"))
        ds.add(DataEntry("entryB", "ts0017", false, "run B.13 fail"))
        ds.add(DataEntry("entryB", "ts0018", false, "run B.14 fail"))

        assertThat(ds.historyToJson()).isEqualTo(
            """
                {
                  "entryA": {
                    "entries": {
                      "ts0014": {"key": "entryA", "ts": "ts0014", "st": true, "d": "run A.11 ok"}, 
                      "ts0013": {"key": "entryA", "ts": "ts0013", "st": true, "d": "run A.10 ok"}, 
                      "ts0012": {"key": "entryA", "ts": "ts0012", "st": true, "d": "run A.09 ok"}, 
                      "ts0011": {"key": "entryA", "ts": "ts0011", "st": true, "d": "run A.08 ok"}, 
                      "ts0010": {"key": "entryA", "ts": "ts0010", "st": true, "d": "run A.07 ok"}, 
                      "ts0009": {"key": "entryA", "ts": "ts0009", "st": true, "d": "run A.06 ok"}, 
                      "ts0008": {"key": "entryA", "ts": "ts0008", "st": true, "d": "run A.05 ok"}, 
                      "ts0007": {"key": "entryA", "ts": "ts0007", "st": true, "d": "run A.04 ok"}, 
                      "ts0006": {"key": "entryA", "ts": "ts0006", "st": true, "d": "run A.03 ok"}, 
                      "ts0003": {"key": "entryA", "ts": "ts0003", "st": true, "d": "run A.02 ok"}
                    }
                  }, 
                  "entryB": {
                    "entries": {
                      "ts0015": {"key": "entryB", "ts": "ts0015", "st": true, "d": "run B.11 ok"}, 
                      "ts0014": {"key": "entryB", "ts": "ts0014", "st": true, "d": "run B.10 ok"}, 
                      "ts0013": {"key": "entryB", "ts": "ts0013", "st": true, "d": "run B.09 ok"}, 
                      "ts0012": {"key": "entryB", "ts": "ts0012", "st": true, "d": "run B.08 ok"}, 
                      "ts0011": {"key": "entryB", "ts": "ts0011", "st": true, "d": "run B.07 ok"}, 
                      "ts0010": {"key": "entryB", "ts": "ts0010", "st": true, "d": "run B.06 ok"}, 
                      "ts0009": {"key": "entryB", "ts": "ts0009", "st": true, "d": "run B.05 ok"}, 
                      "ts0008": {"key": "entryB", "ts": "ts0008", "st": true, "d": "run B.04 ok"}, 
                      "ts0007": {"key": "entryB", "ts": "ts0007", "st": true, "d": "run B.03 ok"}, 
                      "ts0006": {"key": "entryB", "ts": "ts0006", "st": true, "d": "run B.02 ok"}
                    }
                  }
                }
            """.trimIndent())
    }

}
