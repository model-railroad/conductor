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
import com.alflabs.utils.IClock
import com.alflabs.utils.ILogger
import com.alfray.dazzserv.store.DataStore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.annotations.VisibleForTesting
import com.google.errorprone.annotations.concurrent.GuardedBy
import java.net.InetAddress
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Regularly check whether computers have been turned off.
 */
@Singleton
open class DazzOff @Inject constructor(
    private val clock: IClock,
    private val logger: ILogger,
    @Named("IsoUtcDateTime") private val isoDateTimeFormat: DateFormat,
    // DataStore injects DazzOff. Needs a dagger lazy here to remove dagger circular dependency.
    private val store: dagger.Lazy<DataStore>,
) {
    @GuardedBy(value = "computers")
    private val computers = mutableMapOf<String, DazzOffData>()
    private val mapper = jacksonObjectMapper()

    companion object {
        const val TAG = "DazzOff"
        // Number of times to retry the ICMP Ping.
        const val RETRIES = 3
        // Timeout for the local ICMP Ping. Because it's local, a fairly short timeout is enough.
        // Note that the actual time on a computer off is really this timeout x RETRIES.
        const val PING_TIMEOUT_MS = 2_000
        // The key pattern that we monitor. The captured value is typically a valid hostname.
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
                parseEntry(computerName, entry, payload)
            } catch (e: Exception) {
                logger.d(TAG, "Failed to decode DazzOffPayload", e)
            }
        }
    }

    private fun parseEntry(
        computerName: String,
        entry: DataEntry,
        payload: DazzOffPayload,
    ) {
        synchronized(computers) {
            if (!entry.isState) {
                // This computer is off. Don't monitor it.
                computers.remove(computerName)
                return
            } else if (!payload.dazzOff) {
                // This computer is on, but it is not an entry that we should monitor.
                computers.remove(computerName)
                return
            } else {
                // This computer is on and we should monitor it.
                computers[computerName] = DazzOffData(entry, payload)
                logger.d(TAG, "Monitor '$computerName'")
            }
        }
    }

    /**
     * Invoked from the Dazz Scheduler to periodically check if computers are still on.
     */
    fun periodicCheck() {
        // Make a copy of the map so that we don't keep it synchronized, and also
        // because we'll change the original map whilst iterating on it.
        val mapCopy: Map<String, DazzOffData>
        synchronized(computers) {
            mapCopy = computers.toSortedMap()
        }

        mapCopy.forEach { (name, data) ->
            performCheck(name, data)
        }
    }

    private fun performCheck(name: String, data: DazzOffData) {
        logger.d(TAG, "Check '$name'")

        // The ip to monitor is either the one in the payload (if present), or the
        // actual hostname of the computer that was given in the entry key.
        val ip = data.payload.ip ?: name

        repeat(RETRIES) { retry ->
            try {
                // Resolve the IP string or the hostname
                val address = resolveHostname(ip)
                val reachable = ping(address)
                if (reachable) {
                    // Computer still "on". Keep the entry and we'll monitor again later.
                    return
                }
                // A value off just means the given timeout has been reached.
                // We'll still retry N times before giving up.
            } catch (e: Exception) {
                // Exception here should be:
                // - getByName() UnknownHostException – the hostname/ip can't be resolved.
                // - isReachable() IOException – some network error other than ping timeout.
                logger.d(TAG, "Check '$name' (retry $retry) network error", e)
            }
        }
        // If we arrive here, we have failed to ping the host N times for the given timeout.
        // We'll consider it really off, thus changing its state in the DataStore.
        synchronized(computers) {
            computers.remove(name)
        }
        val entry = DataEntry(
            data.entry.key,
            isoDateTimeFormat.format(Date(clock.elapsedRealtime())),
            /*state=*/ false,
            /*payload=*/ null,
        )
        store.get().add(entry)
        logger.d(TAG, "Detected '$name' is off")
    }

    @VisibleForTesting
    internal open fun resolveHostname(ip: String): InetAddress {
        return InetAddress.getByName(ip)
    }

    @VisibleForTesting
    internal open fun ping(address: InetAddress): Boolean {
        return address.isReachable(/*timeout=*/ PING_TIMEOUT_MS)
    }

    internal fun decodePayload(json: String) : DazzOffPayload {
        return mapper.readValue(json, DazzOffPayload::class.java)
    }

    /** Internal data to keep track of this computer for dazz off. */
    internal data class DazzOffData(
        val entry: DataEntry,
        val payload: DazzOffPayload,
    )
}

/** The payload for a "computer/..." DataEntry. */
data class DazzOffPayload(
    @JsonProperty("dazz-off")
    val dazzOff: Boolean = false,
    val ip: String? = null
)
