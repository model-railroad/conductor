package com.alfray.dazzserv

import com.alflabs.utils.ILogger
import com.alflabs.utils.StringLogger
import com.alfray.dazzserv.dagger.DaggerIMainTestComponent
import com.alfray.dazzserv.dagger.IMainComponent
import com.github.ajalt.clikt.testing.test
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

class DazzServTest {
    private lateinit var main : Main
    @Inject lateinit var logger: StringLogger

    @Before
    fun setUp() {
        val component = DaggerIMainTestComponent.factory().createComponent()
        component.inject(this)

        main = object : Main(autoStartServer = false) {
            override fun createComponent(): IMainComponent {
                return component
            }
        }
    }

    @Test
    fun testArgDefaults() {
        val result = main.test("")
        assertThat(main.port).isEqualTo(8080)
        assertThat(logger.string).isEqualTo(
            """
               Main: Configured for 127.0.0.1 port 8080
               Main: End

            """.trimIndent())
        assertThat(result.statusCode).isEqualTo(0)
    }

    @Test
    fun testArgPort() {
        val result = main.test("--port 9090 --host 0.0.0.0")
        assertThat(main.port).isEqualTo(9090)
        assertThat(logger.string).isEqualTo(
            """
               Main: Configured for 0.0.0.0 port 9090
               Main: End

            """.trimIndent())
        assertThat(result.statusCode).isEqualTo(0)
    }
}
