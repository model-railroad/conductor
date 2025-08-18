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

package com.alfray.dazzserv.serv

import com.alflabs.utils.StringLogger
import com.alfray.dazzserv.dagger.DaggerIMainTestComponent
import com.alfray.dazzserv.store.DataStore
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import javax.inject.Inject

class DazzOffTest {
    @Inject lateinit var logger: StringLogger
    private val mockStore = mock<DataStore>()
    private lateinit var dazzOff: DazzOff

    @Suppress("MoveLambdaOutsideParentheses")
    @Before
    fun setUp() {
        val component = DaggerIMainTestComponent.factory().createComponent()
        component.inject(this)

        // Create a version of DazzOff that uses a mock DataStore
        // instead of the real one. All other test components are fakes
        // (and not mocks).
        dazzOff = DazzOff(
            logger,
            // This lambda maps to dagger.Lazy { fun get() = mockStore }.
            { mockStore },
        )
    }

    @Test
    fun testDecodePayload() {
        assertThat(dazzOff.decodePayload(
            """ { "dazz-off": true, "ip": "192.168.0.0" } """
        )).isEqualTo(
            DazzOffPayload(dazzOff = true, ip = "192.168.0.0")
        )
    }
}
