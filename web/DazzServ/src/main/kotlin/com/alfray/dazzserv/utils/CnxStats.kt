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

import com.alflabs.utils.ILogger
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CnxStats @Inject constructor(
    private val logger: ILogger,
) {
    private val numCnx = AtomicInteger(0)
    private var sumBytesIn = AtomicLong(0)
    private var sumBytesOut = AtomicLong(0)
    private var lastLog = -1L

    companion object {
        const val TAG = "CnxStats"
    }

    fun accumulate(bytesIn: Long, bytesOut: Long) {
        numCnx.incrementAndGet()
        sumBytesIn.addAndGet(bytesIn)
        sumBytesOut.addAndGet(bytesOut)
        log()
    }

    fun log(force: Boolean = false) {
        val hashLog = numCnx.get() + sumBytesIn.get() + sumBytesOut.get()
        if (force || hashLog != lastLog) {
            lastLog = hashLog
            logger.d(
                TAG,
                "${numCnx.get()} Cnx. Sum bytes in ${sumBytesIn.get()}, Sum bytes out ${sumBytesOut.get()}"
            )
        }
    }
}
