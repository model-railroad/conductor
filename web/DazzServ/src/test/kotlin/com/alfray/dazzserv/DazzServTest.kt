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

package com.alfray.dazzserv

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
