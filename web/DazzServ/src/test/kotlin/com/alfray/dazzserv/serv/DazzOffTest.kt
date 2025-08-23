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
import com.alflabs.utils.FakeClock
import com.alflabs.utils.StringLogger
import com.alfray.dazzserv.dagger.DaggerIMainTestComponent
import com.alfray.dazzserv.store.DataStore
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import java.io.IOException
import java.net.InetAddress
import java.net.UnknownHostException
import java.text.DateFormat
import javax.inject.Inject
import javax.inject.Named

class DazzOffTest {
    @Inject lateinit var clock: FakeClock
    @Inject lateinit var logger: StringLogger
    @Inject @Named("IsoUtcDateTime") lateinit var isoDateTimeFormat: DateFormat
    private val mockStore = mock<DataStore>()
    private lateinit var dazzOff: DazzOff
    private val resolvedHostnames = mutableListOf<String>()
    private val hostnameToAddress = mutableMapOf<String, InetAddress>()
    private val addressToPing = mutableMapOf<InetAddress, Boolean>()

    @Before
    fun setUp() {
        val component = DaggerIMainTestComponent.factory().createComponent()
        component.inject(this)

        // Create a version of DazzOff that uses a mock DataStore
        // instead of the real one. All other test components are fakes
        // (and not mocks).
        dazzOff = object : DazzOff(
            clock,
            logger,
            isoDateTimeFormat,
            // This lambda maps to dagger.Lazy { fun get() = mockStore }.
            { mockStore },
        ) {
            override fun resolveHostname(ip: String): InetAddress {
                resolvedHostnames.add(ip)
                if (hostnameToAddress.containsKey(ip)) {
                    return hostnameToAddress[ip]!!
                }
                throw UnknownHostException("Unknown host $ip")
            }

            override fun ping(address: InetAddress): Boolean {
                if (addressToPing.containsKey(address)) {
                    return addressToPing[address]!!
                }
                throw IOException("Unknown address $address")
            }
        }
    }

    @Test
    fun testJsonToPayload() {
        assertThat(dazzOff.jsonToPayload(
            """ { "dazz-off": true, "ip": "192.168.0.0" } """
        )).isEqualTo(
            DazzOffPayload(dazzOff = true, ip = "192.168.0.0")
        )

        assertThat(dazzOff.jsonToPayload(
            """ { "dazz-off": true } """
        )).isEqualTo(
            DazzOffPayload(dazzOff = true, ip = null)
        )

        assertThat(dazzOff.jsonToPayload(
            """ { "dazz-off": false } """
        )).isEqualTo(
            DazzOffPayload(dazzOff = false, ip = null)
        )
    }

    @Test
    fun testPayloadToJson() {
        assertThat(dazzOff.payloadToJson(
            DazzOffPayload(dazzOff = true, ip = "192.168.0.0")
        )).isEqualTo(
            """{"dazz-off":true,"ip":"192.168.0.0"}"""
        )

        assertThat(dazzOff.payloadToJson(
            DazzOffPayload(dazzOff = true, ip = null)
        )).isEqualTo(
            """{"dazz-off":true}"""
        )

        assertThat(dazzOff.payloadToJson(
            DazzOffPayload(dazzOff = false, ip = null)
        )).isEqualTo(
            """{"dazz-off":false}"""
        )
    }

    @Test
    fun testMonitor() {
        dazzOff.monitor(DataEntry("non/monitored/key1", "1970-01-01T00:03:54Z", true, null))
        dazzOff.monitor(DataEntry("non/monitored/key2", "1970-01-01T00:03:54Z", false, null))

        dazzOff.monitor(DataEntry("computer/hostname1", "1970-01-01T00:03:54Z", false, null))
        dazzOff.monitor(DataEntry("computer/hostname2", "1970-01-01T00:03:54Z", true,
            """ { "invalid": "payload" } """))
        assertThat(logger.string).contains("DazzOff: Failed to decode DazzOffPayload")

        dazzOff.monitor(DataEntry("computer/hostname3", "1970-01-01T00:03:54Z", true,
            """ { "dazz-off": false } """))
        dazzOff.monitor(DataEntry("computer/hostname4", "1970-01-01T00:03:54Z", true,
            """ { "dazz-off": true, "ip": "192.168.0.0" } """))
        dazzOff.monitor(DataEntry("computer/hostname5", "1970-01-01T00:03:54Z", false,
            """ { "dazz-off": true, "ip": "192.168.0.0" } """))
        assertThat(logger.string).contains("DazzOff: Monitor 'hostname4', currently ON")
        assertThat(logger.string).contains("DazzOff: Monitor 'hostname5', currently OFF")

        verifyNoInteractions(mockStore)
    }

    @Test
    fun testPeriodicCheck_resolveFailed() {
        dazzOff.monitor(DataEntry("computer/hostname0", "1970-01-01T00:03:54Z", false, null))
        dazzOff.monitor(DataEntry("computer/hostname1", "1970-01-01T00:03:54Z", false,
            """ { "dazz-off": true } """))
        dazzOff.monitor(DataEntry("computer/hostname2", "1970-01-01T00:03:54Z", true,
            """ { "dazz-off": true, "ip": "192.168.0.0" } """))

        clock.add(2 * 24 * 3600 * 1000L)
        repeat(7) {
            dazzOff.periodicCheck()
            clock.add(3600 * 1000L)
        }

        assertThat(resolvedHostnames).containsExactly(
            "hostname1",        // 1 retry (same state as currently off)
            "192.168.0.0", "192.168.0.0", "192.168.0.0", "192.168.0.0", "192.168.0.0", // 5 retries
            "hostname1",        // cycle back to trying next host
        ).inOrder()

        verify(mockStore).add(DataEntry(
            "computer/hostname2",
            "1970-01-03T05:00:01Z",
            false,
            """{"dazz-off":true,"ip":"192.168.0.0"}"""))
        verifyNoMoreInteractions(mockStore)

        assertThat(logger.string).contains("DazzOff: Check 'hostname2'; Changed to OFF on try #5")
    }

    @Test
    fun testPeriodicCheck_resolveFailed_pingNegative() {
        dazzOff.monitor(DataEntry("computer/hostname0", "1970-01-01T00:03:54Z", false, null))
        dazzOff.monitor(DataEntry("computer/hostname1", "1970-01-01T00:03:54Z", false,
            """ { "dazz-off": true } """))
        dazzOff.monitor(DataEntry("computer/hostname2", "1970-01-01T00:03:54Z", true,
            """ { "dazz-off": true, "ip": "192.168.0.0" } """))

        clock.add(2 * 24 * 3600 * 1000L)
        hostnameToAddress["hostname1"  ] = mock<InetAddress>()
        hostnameToAddress["192.168.0.0"] = mock<InetAddress>()
        addressToPing[ hostnameToAddress["hostname1"  ]!! ] = false
        addressToPing[ hostnameToAddress["192.168.0.0"]!! ] = false
        repeat(7) {
            dazzOff.periodicCheck()
            clock.add(3600 * 1000L)
        }

        assertThat(resolvedHostnames).containsExactly(
            "hostname1",        // 1 retry (same state as currently off)
            "192.168.0.0", "192.168.0.0", "192.168.0.0", "192.168.0.0", "192.168.0.0", // 5 retries
            "hostname1",        // cycle back to trying next host
        ).inOrder()

        verify(mockStore).add(DataEntry(
            "computer/hostname2",
            "1970-01-03T05:00:01Z",
            false,
            """{"dazz-off":true,"ip":"192.168.0.0"}"""))
        verifyNoMoreInteractions(mockStore)

        assertThat(logger.string).contains("DazzOff: Check 'hostname2'; Changed to OFF on try #5")
    }

    @Test
    fun testPeriodicCheck_resolveFailed_pingPositive() {
        dazzOff.monitor(DataEntry("computer/hostname0", "1970-01-01T00:03:54Z", false, null))
        dazzOff.monitor(DataEntry("computer/hostname1", "1970-01-01T00:03:54Z", false,
            """ { "dazz-off": true } """))
        dazzOff.monitor(DataEntry("computer/hostname2", "1970-01-01T00:03:54Z", true,
            """ { "dazz-off": true, "ip": "192.168.0.0" } """))

        clock.add(2 * 24 * 3600 * 1000L)
        hostnameToAddress["hostname1"  ] = mock<InetAddress>()
        hostnameToAddress["192.168.0.0"] = mock<InetAddress>()
        addressToPing[ hostnameToAddress["hostname1"  ]!! ] = true
        addressToPing[ hostnameToAddress["192.168.0.0"]!! ] = true
        repeat(4) {
            dazzOff.periodicCheck()
            clock.add(3600 * 1000L)
        }

        assertThat(resolvedHostnames).containsExactly(
            "hostname1",        // 1 retry (same state as currently off)
            "192.168.0.0",      // 1 retry (immediately on)
            "hostname1",        // cycle back to trying next host
            "192.168.0.0",      // 1 retry (same state)
        ).inOrder()

        verify(mockStore).add(DataEntry(
            "computer/hostname1",
            "1970-01-03T00:00:01Z",
            true,
            """{"dazz-off":true}"""))
        verifyNoMoreInteractions(mockStore)

        assertThat(logger.string).contains("DazzOff: Check 'hostname1'; Changed to ON on try #1")
    }
}
