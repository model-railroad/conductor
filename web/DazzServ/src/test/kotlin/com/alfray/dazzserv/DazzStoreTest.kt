package com.alfray.dazzserv

import com.alflabs.utils.FakeFileOps
import com.alflabs.utils.StringLogger
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File
import java.nio.charset.StandardCharsets

class DazzStoreTest {
    private val fileOps = FakeFileOps()
    private val logger = StringLogger()
    private val ds = DataStore(logger, fileOps)

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
                      "1970-01-01T00:03:54Z": {"key": "toggles/entry1", "ts": "1970-01-01T00:03:54Z", "st": true, "d": "( some payload )"}, 
                      "1970-01-02T03:04:56Z": {"key": "toggles/entry1", "ts": "1970-01-02T03:04:56Z", "st": false, "d": ""}
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

}
