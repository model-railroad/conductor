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
import com.fasterxml.jackson.annotation.JsonInclude
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
    private var currentIndex = -1
    private var currentRetries = 0
    private var currentData : DazzOffData? = null

    companion object {
        const val TAG = "DazzOff"
        // Number of times to retry the ICMP Ping on failure.
        const val RETRIES = 5
        // Timeout for the local ICMP Ping.
        const val PING_TIMEOUT_MS = 9_000
        // The key pattern that we monitor. The captured value is typically a valid hostname.
        val KEY_RE = "^computer/([A-Za-z0-9-]+)$".toRegex()
    }

    init {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
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

        if (entry.payload.isNullOrEmpty()) {
            return
        }

        KEY_RE.matchEntire(entry.key)?.let { match ->
            try {
                val name = match.groupValues[1]
                val payload = jsonToPayload(entry.payload)
                parseEntry(name, entry, payload)
            } catch (e: Exception) {
                logger.d(TAG, "Failed to decode DazzOffPayload", e)
            }
        }
    }

    private fun parseEntry(
        name: String,
        entry: DataEntry,
        payload: DazzOffPayload,
    ) {
        synchronized(computers) {
            val n = computers.size
            if (!payload.dazzOff) {
                // This is not an entry that we should monitor.
                computers.remove(name)
                return
            } else {
                // Monitor this one.
                computers[name] = DazzOffData(name, entry.isState, entry, payload)
                logger.d(TAG, "Monitor '$name', currently ${if (entry.isState) "ON" else "OFF"}")
            }
            if (n != computers.size) {
                currentData = null
            }
        }
    }

    /**
     * Invoked from the Dazz Scheduler to periodically check if computers are still on.
     */
    fun periodicCheck() {
        synchronized(computers) {
            if (computers.isEmpty()) {
                currentData = null
            } else if (currentData == null) {
                currentIndex = (currentIndex + 1) % computers.size
                val key = computers.keys.toList()[currentIndex]
                currentData = computers[key]
                currentRetries = 0
            }
        }

        currentData?.let {
            performCheck(it)
        }
    }

    private fun performCheck(data: DazzOffData) {
        val name = data.name
        currentRetries++

        // The ip to monitor is either the one in the payload (if present), or the
        // actual hostname of the computer that was given in the entry key.
        val ip = data.payload.ip ?: name

        var state: Boolean
        try {
            // Resolve the IP string or the hostname
            val address = resolveHostname(ip)
            val reachable = ping(address)
            state = reachable
        } catch (e: Exception) {
            // Exception here should be:
            // - getByName() UnknownHostException – the hostname/ip can't be resolved.
            // - isReachable() IOException – some network error other than ping timeout.
            state = false
            logger.d(TAG, "Check '$name' (retry $currentRetries) network error", e)
        }

        if (state == data.state) {
            // Nothing changed. Move to the next check.
            currentData = null
            // This gets very verbose and doesn't add much useful information since it's a no-op
            // logger.d(TAG, "Check '$name'; Unchanged on try #$currentRetries")
            return
        }

        if (state || currentRetries >= RETRIES) {
            // Compute is now on, or it's off after N retries... accept the change.
            currentData = null
            synchronized(computers) {
                computers[name] = data.copy(state = state)
            }
            val entry = DataEntry(
                data.entry.key,
                isoDateTimeFormat.format(Date(clock.elapsedRealtime())),
                state,
                payloadToJson(data.payload),
            )
            store.get().add(entry)
            logger.d(TAG, "Check '$name'; Changed to ${if (state) "ON" else "OFF"} on try #$currentRetries")
        } else {
            // State is off but we haven't reached the retry count yet
            logger.d(TAG, "Check '$name'; Noticed is OFF on try #$currentRetries")
        }
    }

    @VisibleForTesting
    internal open fun resolveHostname(ip: String): InetAddress {
        return InetAddress.getByName(ip)
    }

    @VisibleForTesting
    internal open fun ping(address: InetAddress): Boolean {
        return address.isReachable(/*timeout=*/ PING_TIMEOUT_MS)
    }

    internal fun jsonToPayload(json: String) : DazzOffPayload {
        return mapper.readValue(json, DazzOffPayload::class.java)
    }

    internal fun payloadToJson(payload: DazzOffPayload): String {
        return mapper.writeValueAsString(payload)
    }

    /** Internal data to keep track of this computer for dazz off. */
    internal data class DazzOffData(
        val name: String,
        val state: Boolean,
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
