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

package com.alfray.dazzserv.store

import com.alflabs.utils.FileOps
import com.alflabs.utils.IClock
import com.alflabs.utils.ILogger
import com.alflabs.utils.ThreadLoop
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DazzSched @Inject constructor(
    private val logger: ILogger,
    private val clock: IClock,
    private val fileOps: FileOps,
) : ThreadLoop() {

    companion object {
        const val TAG = "DazzSched"
        const val IDLE_SLEEP_MS: Long = 1000L * 10L
    }

    /// Returns false if the directory does not exist.
    fun setAndCheckStoreDir(storeDir: String): Boolean {
        // TBD
        return true
    }

    /// Load stored on-disk data.
    fun load() {
        // TBD
    }

    override fun start() {
        super.start("DazzSched")
    }

    /**
     * Requests termination. Pending tasks will be executed, no new task is allowed.
     * Waiting time is 10 seconds max.
     */
    override fun stop() {
        logger.d(TAG, "Stop")
        try {
            super.stopWithPreTimeout(10, TimeUnit.SECONDS)
        } catch (e: Exception) {
            logger.d(TAG, "Stop Exception", e)
        }
        logger.d(TAG, "Stopped")
    }

    override fun _runInThreadLoop() {
        // TBD

        try {
            logger.d(TAG, "Loop")
            Thread.sleep(IDLE_SLEEP_MS)
        } catch (e: Exception) {
            logger.d(TAG, "Stats idle loop interrupted", e)
        }
    }
}
