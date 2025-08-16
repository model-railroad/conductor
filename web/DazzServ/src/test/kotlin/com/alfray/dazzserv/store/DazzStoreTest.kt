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
import com.alflabs.utils.StringLogger
import com.alfray.dazzserv.dagger.DaggerIMainTestComponent
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.charset.StandardCharsets
import javax.inject.Inject


class DazzStoreTest {
    @Inject lateinit var logger: StringLogger
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

        val file = File("tmp", "save.dat")
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
    fun testSaveAndLoad() {
        ds.store(""" {"key": "toggles/entry1", "ts": "1970-01-01T00:05:67Z", "st": false, "d": "payload 1"} """)
        ds.store(""" {"key": "toggles/entry2", "ts": "1970-01-02T00:06:78Z", "st": true , "d": "payload 2"} """)
        ds.store(""" {"key": "toggles/entry2", "ts": "1970-01-03T00:07:89Z", "st": false, "d": "payload 3"} """)
        ds.store(""" {"key": "toggles/entry1", "ts": "1970-01-04T00:08:90Z", "st": true , "d": "payload 4"} """)

        val file = File("tmp", "save.dat")
        ds.saveTo(file)

        assertThat(fileOps.isFile(file)).isTrue()
        assertThat(fileOps.toString(file, StandardCharsets.UTF_8)).isEqualTo(
            """
                {"key":"toggles/entry1","ts":"1970-01-01T00:05:67Z","st":false,"d":"payload 1"}
                {"key":"toggles/entry2","ts":"1970-01-02T00:06:78Z","st":true,"d":"payload 2"}
                {"key":"toggles/entry2","ts":"1970-01-03T00:07:89Z","st":false,"d":"payload 3"}
                {"key":"toggles/entry1","ts":"1970-01-04T00:08:90Z","st":true,"d":"payload 4"}

            """.trimIndent()
        )

        val newDataStore = DataStore(logger, fileOps)
        assertThat(newDataStore.storeToJson()).isEqualTo("{ }")

        newDataStore.loadFrom(file)
        assertThat(newDataStore.storeToJson()).isEqualTo(
            """
                {
                  "toggles/entry1": {
                    "entries": {
                      "1970-01-04T00:08:90Z": {"key": "toggles/entry1", "ts": "1970-01-04T00:08:90Z", "st": true, "d": "payload 4"}, 
                      "1970-01-01T00:05:67Z": {"key": "toggles/entry1", "ts": "1970-01-01T00:05:67Z", "st": false, "d": "payload 1"}
                    }
                  }, 
                  "toggles/entry2": {
                    "entries": {
                      "1970-01-03T00:07:89Z": {"key": "toggles/entry2", "ts": "1970-01-03T00:07:89Z", "st": false, "d": "payload 3"}, 
                      "1970-01-02T00:06:78Z": {"key": "toggles/entry2", "ts": "1970-01-02T00:06:78Z", "st": true, "d": "payload 2"}
                    }
                  }
                }
            """.trimIndent()
        )

        // Reloading the same stuff is indempotent
        ds.loadFrom(file)
        assertThat(ds.storeToJson()).isEqualTo(newDataStore.storeToJson())
    }

    @Test
    fun testPurgeOlderEntriesThan() {
        ds.add(DataEntry("toggles/entryA", "1970-01-01T00:01:00Z", true, "payload 1"))
        ds.add(DataEntry("toggles/entryA", "1970-01-01T00:02:00Z", true, "payload 2"))
        ds.add(DataEntry("toggles/entryA", "1970-01-02T00:03:00Z", true, "payload 3"))
        ds.add(DataEntry("toggles/entryB", "1970-01-02T00:04:00Z", true, "payload 4"))
        ds.add(DataEntry("toggles/entryB", "1970-01-03T00:05:00Z", true, "payload 5"))
        ds.add(DataEntry("toggles/entryB", "1970-01-03T00:06:00Z", true, "payload 6"))
        ds.add(DataEntry("toggles/entryA", "1970-01-03T00:07:00Z", true, "payload 7"))
        ds.add(DataEntry("toggles/entryC", "1970-01-04T00:08:00Z", true, "payload 8"))
        ds.add(DataEntry("toggles/entryC", "1970-01-04T00:09:00Z", true, "payload 9"))
        ds.add(DataEntry("toggles/entryA", "1970-01-04T00:10:00Z", true, "payload A"))
        ds.add(DataEntry("toggles/entryC", "1970-01-05T00:11:00Z", true, "payload B"))
        ds.add(DataEntry("toggles/entryA", "1970-01-05T00:12:00Z", true, "payload C"))
        ds.add(DataEntry("toggles/entryB", "1970-01-06T00:13:00Z", true, "payload D"))
        ds.add(DataEntry("toggles/entryC", "1970-01-06T00:14:00Z", true, "payload E"))
        ds.add(DataEntry("toggles/entryC", "INVALID-07T00:15:00Z", true, "invalid timestamp 1"))
        ds.add(DataEntry("toggles/entryC", "1970-01-06 00:16:00" , true, "invalid timestamp 2"))
        ds.add(DataEntry("toggles/entryC", "19700106001500"      , true, "invalid timestamp 3"))

        val initial = ds.storeToJson()
        ds.purgeOlderEntriesThan(numDaysToKeep = 10)
        assertThat(ds.storeToJson()).isEqualTo(initial)
        assertThat(logger.string).isEqualTo("")

        ds.purgeOlderEntriesThan(numDaysToKeep = 4) // keeps days -06, -05, -04, -03
        assertThat(logger.string).contains("DataStore: Purged 4 older entries")
        assertThat(ds.storeToJson()).isEqualTo(
            """
                {
                  "toggles/entryA": {
                    "entries": {
                      "1970-01-05T00:12:00Z": {"key": "toggles/entryA", "ts": "1970-01-05T00:12:00Z", "st": true, "d": "payload C"}, 
                      "1970-01-04T00:10:00Z": {"key": "toggles/entryA", "ts": "1970-01-04T00:10:00Z", "st": true, "d": "payload A"}, 
                      "1970-01-03T00:07:00Z": {"key": "toggles/entryA", "ts": "1970-01-03T00:07:00Z", "st": true, "d": "payload 7"}
                    }
                  }, 
                  "toggles/entryB": {
                    "entries": {
                      "1970-01-06T00:13:00Z": {"key": "toggles/entryB", "ts": "1970-01-06T00:13:00Z", "st": true, "d": "payload D"}, 
                      "1970-01-03T00:06:00Z": {"key": "toggles/entryB", "ts": "1970-01-03T00:06:00Z", "st": true, "d": "payload 6"}, 
                      "1970-01-03T00:05:00Z": {"key": "toggles/entryB", "ts": "1970-01-03T00:05:00Z", "st": true, "d": "payload 5"}
                    }
                  }, 
                  "toggles/entryC": {
                    "entries": {
                      "INVALID-07T00:15:00Z": {"key": "toggles/entryC", "ts": "INVALID-07T00:15:00Z", "st": true, "d": "invalid timestamp 1"}, 
                      "19700106001500": {"key": "toggles/entryC", "ts": "19700106001500", "st": true, "d": "invalid timestamp 3"}, 
                      "1970-01-06T00:14:00Z": {"key": "toggles/entryC", "ts": "1970-01-06T00:14:00Z", "st": true, "d": "payload E"}, 
                      "1970-01-06 00:16:00": {"key": "toggles/entryC", "ts": "1970-01-06 00:16:00", "st": true, "d": "invalid timestamp 2"}, 
                      "1970-01-05T00:11:00Z": {"key": "toggles/entryC", "ts": "1970-01-05T00:11:00Z", "st": true, "d": "payload B"}, 
                      "1970-01-04T00:09:00Z": {"key": "toggles/entryC", "ts": "1970-01-04T00:09:00Z", "st": true, "d": "payload 9"}, 
                      "1970-01-04T00:08:00Z": {"key": "toggles/entryC", "ts": "1970-01-04T00:08:00Z", "st": true, "d": "payload 8"}
                    }
                  }
                }
            """.trimIndent()
        )

        ds.purgeOlderEntriesThan(numDaysToKeep = 2) // keeps days -06, -05
        assertThat(logger.string).contains("DataStore: Purged 6 older entries")
        assertThat(ds.storeToJson()).isEqualTo(
            """
                {
                  "toggles/entryA": {
                    "entries": {
                      "1970-01-05T00:12:00Z": {"key": "toggles/entryA", "ts": "1970-01-05T00:12:00Z", "st": true, "d": "payload C"}
                    }
                  }, 
                  "toggles/entryB": {
                    "entries": {
                      "1970-01-06T00:13:00Z": {"key": "toggles/entryB", "ts": "1970-01-06T00:13:00Z", "st": true, "d": "payload D"}
                    }
                  }, 
                  "toggles/entryC": {
                    "entries": {
                      "INVALID-07T00:15:00Z": {"key": "toggles/entryC", "ts": "INVALID-07T00:15:00Z", "st": true, "d": "invalid timestamp 1"}, 
                      "19700106001500": {"key": "toggles/entryC", "ts": "19700106001500", "st": true, "d": "invalid timestamp 3"}, 
                      "1970-01-06T00:14:00Z": {"key": "toggles/entryC", "ts": "1970-01-06T00:14:00Z", "st": true, "d": "payload E"}, 
                      "1970-01-06 00:16:00": {"key": "toggles/entryC", "ts": "1970-01-06 00:16:00", "st": true, "d": "invalid timestamp 2"}, 
                      "1970-01-05T00:11:00Z": {"key": "toggles/entryC", "ts": "1970-01-05T00:11:00Z", "st": true, "d": "payload B"}
                    }
                  }
                }
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

        assertThat(ds.queryToJson("blah")).isEqualTo("{ }")
        assertThat(ds.queryToJson("toggles/")).isEqualTo("{ }")

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

        assertThat(ds.liveToJson(keyQuery = "foo")).isEqualTo("{ }")

        assertThat(ds.liveToJson(keyQuery = "")).isEqualTo(
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

        assertThat(ds.liveToJson(keyQuery = "foo")).isEqualTo("{ }")

        assertThat(ds.historyToJson(keyQuery = "")).isEqualTo(
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
