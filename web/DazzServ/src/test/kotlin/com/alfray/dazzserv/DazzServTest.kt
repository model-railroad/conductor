package com.alfray.dazzserv

import com.alflabs.utils.FakeFileOps
import com.github.ajalt.clikt.testing.test
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DazzServTest {
    private val fileOps = FakeFileOps()

    @Test
    fun testArgDefaults() {
        val ds = DazzServ(fileOps, autoStartServer = false)
        val result = ds.test("")
        assertThat(ds.port).isEqualTo(8080)
        assertThat(result.stdout).isEqualTo(
            """
               DazzServ: Configured for 127.0.0.1 port 8080
               DazzServ: End

            """.trimIndent())
        assertThat(result.statusCode).isEqualTo(0)
    }

    @Test
    fun testArgPort() {
        val ds = DazzServ(fileOps, autoStartServer = false)
        val result = ds.test("--port 9090 --host 0.0.0.0")
        assertThat(ds.port).isEqualTo(9090)
        assertThat(result.stdout).isEqualTo(
            """
               DazzServ: Configured for 0.0.0.0 port 9090
               DazzServ: End

            """.trimIndent())
        assertThat(result.statusCode).isEqualTo(0)
    }
}
