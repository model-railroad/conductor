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

package com.alfray.dazzserv.utils

import com.alflabs.utils.StringLogger
import com.alfray.dazzserv.dagger.DaggerIMainTestComponent
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

class CnxStatsTest {
    @Inject lateinit var logger: StringLogger
    @Inject lateinit var cnxStats: CnxStats

    @Before
    fun setUp() {
        val component = DaggerIMainTestComponent.factory().createComponent()
        component.inject(this)
    }

    @Test
    fun testAccumulateAndLog() {
        cnxStats.accumulate("label1", 11, 31)
        cnxStats.accumulate("label2", 13, 37)
        cnxStats.accumulate("label2", 17, 41)
        cnxStats.accumulate("label1", 19, 47)

        assertThat(logger.string).isEqualTo(
            """
                CnxStats: [label1] 1 requests, 11 bytes in, 31 bytes out
                CnxStats: [label2] 1 requests, 13 bytes in, 37 bytes out
                CnxStats: [label2] 2 requests, 30 bytes in, 78 bytes out
                CnxStats: [label1] 2 requests, 30 bytes in, 78 bytes out

            """.trimIndent()
        )

        logger.clear()
        cnxStats.log()
        assertThat(logger.string).isEqualTo(
            """
                CnxStats: [label1] 2 requests, 30 bytes in, 78 bytes out
                CnxStats: [label2] 2 requests, 30 bytes in, 78 bytes out

            """.trimIndent()
        )
    }
}
