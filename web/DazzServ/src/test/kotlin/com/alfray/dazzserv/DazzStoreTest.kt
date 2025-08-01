package com.alfray.dazzserv

import com.alflabs.utils.StringLogger
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DazzStoreTest {
    private val logger = StringLogger()

    @Test
    fun testSerializeDataEntry() {
        val ds = DataStore(logger)
        val entry = DataEntry("toggles/entry1", "1970-01-01T00:03:54Z", true, "( some payload )")
        val json = ds.entryToJson(entry)

        assertThat(json).isEqualTo(
            """
                {"key":"toggles/entry1","ts":"1970-01-01T00:03:54Z","st":true,"d":"( some payload )"}
            """.trimIndent())
    }

    @Test
    fun testSerializeDataStore() {
        val ds = DataStore(logger)

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
                      "1970-01-01T00:04:37Z": {"key": "toggles/entry1", "ts": "1970-01-01T00:04:37Z", "st": true, "d": "payload 3"}, 
                      "1970-01-04T00:06:59Z": {"key": "toggles/entry1", "ts": "1970-01-04T00:06:59Z", "st": true, "d": "payload 1"}
                    }
                  }, 
                  "toggles/entry2": {
                    "entries": {
                      "1970-01-02T00:03:26Z": {"key": "toggles/entry2", "ts": "1970-01-02T00:03:26Z", "st": true, "d": "payload 4"}, 
                      "1970-01-03T00:05:48Z": {"key": "toggles/entry2", "ts": "1970-01-03T00:05:48Z", "st": true, "d": "payload 2"}
                    }
                  }
                }
            """.trimIndent())
    }

}
