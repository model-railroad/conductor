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

import com.alflabs.utils.FakeClock
import com.alflabs.utils.FakeFileOps
import com.alflabs.utils.StringLogger
import com.alfray.dazzserv.dagger.DaggerIMainTestComponent
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import java.io.File
import javax.inject.Inject

class CnxStatsTest {
    @Inject lateinit var clock: FakeClock
    @Inject lateinit var logger: StringLogger
    @Inject lateinit var fileOps: FakeFileOps
    @Inject lateinit var cnxStats: CnxStats

    @Before
    fun setUp() {
        val component = DaggerIMainTestComponent.factory().createComponent()
        component.inject(this)
    }

    @Test
    fun testAccumulateAndLog() {
        clock.add( 2 * 24 * 3600L * 1000L)
        cnxStats.accumulate("label1", 11, 31)
        cnxStats.accumulate("label2", 13, 37)
        cnxStats.accumulate("label2", 17, 41)
        cnxStats.accumulate("label1", 19, 47)
        clock.add(30 * 24 * 3600L * 1000L)
        cnxStats.accumulate("label1", 11, 41)
        cnxStats.accumulate("label2", 12, 42)

        assertThat(logger.string).isEqualTo(
            """
                CnxStats: 1970-01-03 [label1] 1 requests, 11 bytes in, 31 bytes out
                CnxStats: 1970-01-03 [label2] 1 requests, 13 bytes in, 37 bytes out
                CnxStats: 1970-01-03 [label2] 2 requests, 30 bytes in, 78 bytes out
                CnxStats: 1970-01-03 [label1] 2 requests, 30 bytes in, 78 bytes out
                CnxStats: 1970-02-02 [label1] 1 requests, 11 bytes in, 41 bytes out
                CnxStats: 1970-02-02 [label2] 1 requests, 12 bytes in, 42 bytes out

            """.trimIndent()
        )

        logger.clear()
        cnxStats.logDays()
        assertThat(logger.string).isEqualTo(
            """
                CnxStats: 1970-01-03 [label1] 2 requests, 30 bytes in, 78 bytes out
                CnxStats: 1970-01-03 [label2] 2 requests, 30 bytes in, 78 bytes out
                CnxStats: 1970-02-02 [label1] 1 requests, 11 bytes in, 41 bytes out
                CnxStats: 1970-02-02 [label2] 1 requests, 12 bytes in, 42 bytes out

            """.trimIndent()
        )

        logger.clear()
        cnxStats.logMonths()
        assertThat(logger.string).isEqualTo(
            """
                CnxStats: 1970-01 [label1] 2 requests, 30 bytes in, 78 bytes out
                CnxStats: 1970-01 [label2] 2 requests, 30 bytes in, 78 bytes out
                CnxStats: 1970-02 [label1] 1 requests, 11 bytes in, 41 bytes out
                CnxStats: 1970-02 [label2] 1 requests, 12 bytes in, 42 bytes out

            """.trimIndent()
        )

        assertThat(cnxStats.logToString()).isEqualTo(
            """
                Days:
                
                1970-01-03 [label1] 2 requests, 30 bytes in, 78 bytes out
                1970-01-03 [label2] 2 requests, 30 bytes in, 78 bytes out
                
                1970-02-02 [label1] 1 requests, 11 bytes in, 41 bytes out
                1970-02-02 [label2] 1 requests, 12 bytes in, 42 bytes out
                
                Months:
                
                1970-01 [label1] 2 requests, 30 bytes in, 78 bytes out
                1970-01 [label2] 2 requests, 30 bytes in, 78 bytes out
                
                1970-02 [label1] 1 requests, 11 bytes in, 41 bytes out
                1970-02 [label2] 1 requests, 12 bytes in, 42 bytes out
                

            """.trimIndent()
        )

        // create the storage dir
        val storeDir = fileOps.toFile("tmp", "testdir")
        fileOps.writeBytes("dummy".toByteArray(Charsets.UTF_8), File(storeDir, "dummy"))

        // write to the save file
        logger.clear()
        cnxStats.save(storeDir.path)

        // verify write
        assertThat(
            fileOps
                .toString(File(storeDir, "cnxstats.json"), Charsets.UTF_8)
                .replace("\r\n", "\n")
        ).isEqualTo(
            """
                {
                  "daysMap" : {
                    "1970-01-03" : {
                      "date" : "1970-01-03",
                      "map" : {
                        "label1" : {
                          "numRequests" : 2,
                          "sumBytesIn" : 30,
                          "sumBytesOut" : 78
                        },
                        "label2" : {
                          "numRequests" : 2,
                          "sumBytesIn" : 30,
                          "sumBytesOut" : 78
                        }
                      }
                    },
                    "1970-02-02" : {
                      "date" : "1970-02-02",
                      "map" : {
                        "label1" : {
                          "numRequests" : 1,
                          "sumBytesIn" : 11,
                          "sumBytesOut" : 41
                        },
                        "label2" : {
                          "numRequests" : 1,
                          "sumBytesIn" : 12,
                          "sumBytesOut" : 42
                        }
                      }
                    }
                  },
                  "monthsMap" : {
                    "1970-01" : {
                      "date" : "1970-01",
                      "map" : {
                        "label1" : {
                          "numRequests" : 2,
                          "sumBytesIn" : 30,
                          "sumBytesOut" : 78
                        },
                        "label2" : {
                          "numRequests" : 2,
                          "sumBytesIn" : 30,
                          "sumBytesOut" : 78
                        }
                      }
                    },
                    "1970-02" : {
                      "date" : "1970-02",
                      "map" : {
                        "label1" : {
                          "numRequests" : 1,
                          "sumBytesIn" : 11,
                          "sumBytesOut" : 41
                        },
                        "label2" : {
                          "numRequests" : 1,
                          "sumBytesIn" : 12,
                          "sumBytesOut" : 42
                        }
                      }
                    }
                  }
                }
            """.trimIndent()
        )

        // try a reload
        cnxStats.load(storeDir.path)

        assertThat(logger.string.replace('\\', '/')).isEqualTo(
            """
                CnxStats: Stored tmp/testdir/cnxstats.json
                CnxStats: Loaded tmp/testdir/cnxstats.json

            """.trimIndent()
        )
    }
}
