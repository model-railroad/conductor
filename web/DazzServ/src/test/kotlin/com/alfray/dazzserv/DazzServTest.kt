package com.alfray.dazzserv

import com.github.ajalt.clikt.testing.test
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DazzServTest {
    @Test
    fun testArgDefaults() {
        val ds = DazzServ()
        val result = ds.test("")
        assertThat(ds.port).isEqualTo(8080)
        assertThat(result.stdout).isEqualTo("DazzServ running on port 8080\n")
        assertThat(result.statusCode).isEqualTo(0)
    }

    @Test
    fun testArgPort() {
        val ds = DazzServ()
        val result = ds.test("--port 9090")
        assertThat(ds.port).isEqualTo(9090)
        assertThat(result.stdout).isEqualTo("DazzServ running on port 9090\n")
        assertThat(result.statusCode).isEqualTo(0)
    }
}
