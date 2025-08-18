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

import com.alflabs.dazzserv.store.DataEntry
import com.alflabs.utils.ILogger
import com.alfray.dazzserv.store.DataStore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.errorprone.annotations.concurrent.GuardedBy
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Regularly check whether computers have been turned off.
 */
@Singleton
class DazzOff @Inject constructor(
    private val logger: ILogger,
    // DataStore injects DazzOff. Need a lazy<> to remove circular dependency.
    private val store: dagger.Lazy<DataStore>,
) {
    @GuardedBy(value = "computers")
    private val computers = mutableMapOf<String, DazzOffPayload>()
    private val mapper = jacksonObjectMapper()

    companion object {
        const val TAG = "DazzOff"
        val KEY_RE = "^computer/([A-Za-z0-9-]+)$".toRegex()
    }

    /**
     * Invoked from the REST Handler thread (via DataStore) to check whether new data entries
     * are computer entries to monitor.
     */
    fun monitor(entry: DataEntry) {
        // Monitor entries for the following patterns:
        // key: must be computer/<name> (alphanumeric with -, no other special characters).
        // state: true if computer is on.
        // payload: must be a DazzOffPayload with a field dazz-off=true.

        if (!entry.isState || entry.payload.isNullOrEmpty()) {
            return
        }

        KEY_RE.matchEntire(entry.key)?.let { match ->
            try {
                val computerName = match.groupValues[1]
                val payload = decodePayload(entry.payload)
                if (!payload.dazzOff) {
                    // This is not an entry that we should monitor.
                    synchronized(computers) {
                        computers.remove(computerName)
                    }
                    return
                }
                synchronized(computers) {
                    if (!entry.isState) {
                        // This computer is off. Don't monitor it.
                        computers.remove(computerName)
                        return
                    } else {
                        // This computer is on. Do monitor it.
                        computers[computerName] = payload
                        logger.d(TAG, "Monitor '$computerName'")
                    }
                }
            } catch (e: Exception) {
                logger.d(TAG, "Failed to decode DazzOffPayload", e)
            }
        }
    }

    /**
     * Invoked from the Dazz Scheduler to periodically check if computers are still on.
     */
    fun periodicCheck() {
        val mapCopy: Map<String, DazzOffPayload>
        synchronized(computers) {
            mapCopy = computers.toSortedMap()
        }

        mapCopy.forEach { (name, payload) ->
            performCheck(name, payload)
        }
    }

    private fun performCheck(name: String, payload: DazzOffPayload) {
        logger.d(TAG, "Check '$name'")
    }

    internal fun decodePayload(json: String) : DazzOffPayload {
        return mapper.readValue(json, DazzOffPayload::class.java)
    }
}

data class DazzOffPayload(
    @JsonProperty("dazz-off")
    val dazzOff: Boolean = false,
    val ip: String? = null
)
