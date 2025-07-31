package com.alfray.dazzserv

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DazzStoreTest {

    @Test
    fun testSerializeDataEntry() {
        val entry = DataEntry("toggles/passenger", "1970-01-01T00:03:54Z", true, "( some payload )")
        val json = entry.toJson()

        assertThat(json).isEqualTo(
            """
                {"key":"toggles/passenger","ts":"1970-01-01T00:03:54Z","st":true,"d":"( some payload )"}
            """.trimIndent())
    }

}
