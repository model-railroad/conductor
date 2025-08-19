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

import com.alflabs.utils.FileOps
import com.alflabs.utils.IClock
import com.alflabs.utils.ILogger
import com.alflabs.utils.ThreadLoop
import com.alfray.dazzserv.store.DataStore
import com.google.common.annotations.VisibleForTesting
import java.io.File
import java.text.DateFormat
import java.util.Collections
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class DazzSched @Inject constructor(
    private val logger: ILogger,
    private val clock: IClock,
    private val fileOps: FileOps,
    private val store: DataStore,
    private val dazzOff: DazzOff,
    @Named("IsoDateOnly") private val isoDateOnlyFormat: DateFormat,
) : ThreadLoop() {
    private var nextOffTS: Long = 0
    private var nextSaveTS: Long = 0
    private var nextPurgeTS: Long = 0
    private lateinit var storeDir: File

    companion object {
        const val TAG = "DazzSched"
        const val IDLE_SLEEP_MS = 1000L * 10L
        const val SAVE_INTERVAL_SEC = 30            // Default is to save every 30 seconds
        const val OFF_INTERVAL_SEC = 60             // Default DazzOff is every 1 minute
        const val PURGE_INTERVAL_SEC = 12*3600      // Default to purge old data is every 12 hours
        const val LOAD_NUM_DAYS = 7                 // Number of last days to reload on start
        const val PURGE_NUM_DAYS = 7                // Number of days to keep when purging
    }

    /// Returns false if the directory does not exist.
    @Suppress("LocalVariableName")
    fun setAndCheckStoreDir(storeDir_: String): Boolean {
        scheduleNextOff()
        scheduleNextSave()
        scheduleNextPurge()
        storeDir = File(storeDir_)
        if (storeDir_.startsWith("~") && !fileOps.isDir(storeDir) && !fileOps.isFile(storeDir)) {
            storeDir = File(System.getProperty("user.home"), storeDir_.substring(1))
        }

        if (!fileOps.isDir(this.storeDir)) {
            logger.d(TAG, """ERROR: Store directory '$storeDir' does not exist.
                |      SUGGESTION: Create it first or select an existing directory with --store-dir.
                |""".trimMargin())
            return false
        }
        return true
    }

    /// Load stored on-disk data.
    fun load() {
        try {
            val files = fileOps.listDirectory(
                storeDir,
                "ds_*.txt",
                /*listFiles=*/ true,
                /*listDirectories=*/ false) as ArrayList<File>
            Collections.sort(files, Collections.reverseOrder())

            var n = LOAD_NUM_DAYS
            for (file in files) {
                store.loadFrom(file)
                n--
                if (n <= 0) break
            }
        } catch (e: Exception) {
            logger.d(TAG, "Load failed", e)
        }
    }

    private fun scheduleNextOff() {
        nextOffTS = clock.elapsedRealtime() + OFF_INTERVAL_SEC * 1000L
    }

    private fun scheduleNextSave() {
        nextSaveTS = clock.elapsedRealtime() + SAVE_INTERVAL_SEC * 1000L
    }

    private fun scheduleNextPurge() {
        nextPurgeTS = clock.elapsedRealtime() + PURGE_INTERVAL_SEC * 1000L
    }

    override fun start() {
        scheduleNextOff()
        scheduleNextSave()
        scheduleNextPurge()
        super.start("DazzSched")
        logger.d(TAG, "Start")
    }

    /**
     * Requests termination. Pending tasks will be executed, no new task is allowed.
     * Waiting time is 10 seconds max.
     */
    override fun stop() {
        logger.d(TAG, "Stop")
        try {
            super.stopWithPreTimeout(2, TimeUnit.SECONDS)
        } catch (e: Exception) {
            logger.d(TAG, "Stop Exception", e)
        }
        doSave()
        logger.d(TAG, "Stopped")
    }

    override fun _runInThreadLoop() {
        try {
            val nowTS = clock.elapsedRealtime()

            if (nowTS >= nextSaveTS) {
                doSave()
            } else if (nowTS >= nextPurgeTS) {
                doPurge()
            } else if (nowTS >= nextOffTS) {
                doOff()
            }

            Thread.sleep(IDLE_SLEEP_MS)
        } catch (e: Exception) {
            logger.d(TAG, "Loop interrupted", e)
        }
    }

    @VisibleForTesting
    internal fun doSave() {
        store.saveTo(fileForTimestamp(nextSaveTS))
        scheduleNextSave()
    }

    private fun doPurge() {
        store.purgeOlderEntriesThan(PURGE_NUM_DAYS)
        scheduleNextPurge()
    }

    private fun doOff() {
        dazzOff.periodicCheck()
        scheduleNextOff()
    }

    @VisibleForTesting
    internal fun fileForTimestamp(timestampMs: Long): File {
        val date = Date(timestampMs)
        val isoTimestamp: String = isoDateOnlyFormat.format(date)
        val file = File(storeDir, "ds_${isoTimestamp}.txt")
        return file
    }
}
