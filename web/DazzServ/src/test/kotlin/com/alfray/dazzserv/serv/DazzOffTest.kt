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
    fun testDecodePayload() {
        assertThat(dazzOff.decodePayload(
            """ { "dazz-off": true, "ip": "192.168.0.0" } """
        )).isEqualTo(
            DazzOffPayload(dazzOff = true, ip = "192.168.0.0")
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
        assertThat(logger.string).contains("DazzOff: Monitor 'hostname4'")

        verifyNoInteractions(mockStore)
    }

    @Test
    fun testPeriodicCheck_resolveFailed() {
        dazzOff.monitor(DataEntry("computer/hostname0", "1970-01-01T00:03:54Z", false, null))
        dazzOff.monitor(DataEntry("computer/hostname1", "1970-01-01T00:03:54Z", true,
            """ { "dazz-off": true } """))
        dazzOff.monitor(DataEntry("computer/hostname2", "1970-01-01T00:03:54Z", true,
            """ { "dazz-off": true, "ip": "192.168.0.0" } """))

        clock.add(2 * 24 * 3600 * 1000L)
        dazzOff.periodicCheck()

        assertThat(resolvedHostnames).containsExactly(
            "hostname1",   "hostname1",   "hostname1",   // 3 retries
            "192.168.0.0", "192.168.0.0", "192.168.0.0", // 3 retries
        ).inOrder()

        verify(mockStore).add(DataEntry("computer/hostname1", "1970-01-03T00:00:01Z", false, null))
        verify(mockStore).add(DataEntry("computer/hostname2", "1970-01-03T00:00:01Z", false, null))
        verifyNoMoreInteractions(mockStore)

        assertThat(logger.string).contains("DazzOff: Detected 'hostname1' is off")
        assertThat(logger.string).contains("DazzOff: Detected 'hostname2' is off")
    }

    @Test
    fun testPeriodicCheck_resolveFailed_pingNegative() {
        dazzOff.monitor(DataEntry("computer/hostname0", "1970-01-01T00:03:54Z", false, null))
        dazzOff.monitor(DataEntry("computer/hostname1", "1970-01-01T00:03:54Z", true,
            """ { "dazz-off": true } """))
        dazzOff.monitor(DataEntry("computer/hostname2", "1970-01-01T00:03:54Z", true,
            """ { "dazz-off": true, "ip": "192.168.0.0" } """))

        clock.add(2 * 24 * 3600 * 1000L)
        hostnameToAddress["hostname1"  ] = mock<InetAddress>()
        hostnameToAddress["192.168.0.0"] = mock<InetAddress>()
        addressToPing[ hostnameToAddress["hostname1"  ]!! ] = false
        addressToPing[ hostnameToAddress["192.168.0.0"]!! ] = false

        dazzOff.periodicCheck()

        assertThat(resolvedHostnames).containsExactly(
            "hostname1",   "hostname1",   "hostname1",   // 3 retries
            "192.168.0.0", "192.168.0.0", "192.168.0.0", // 3 retries
        ).inOrder()

        verify(mockStore).add(DataEntry("computer/hostname1", "1970-01-03T00:00:01Z", false, null))
        verify(mockStore).add(DataEntry("computer/hostname2", "1970-01-03T00:00:01Z", false, null))
        verifyNoMoreInteractions(mockStore)

        assertThat(logger.string).contains("DazzOff: Detected 'hostname1' is off")
        assertThat(logger.string).contains("DazzOff: Detected 'hostname2' is off")
    }

    @Test
    fun testPeriodicCheck_resolveFailed_pingPositive() {
        dazzOff.monitor(DataEntry("computer/hostname0", "1970-01-01T00:03:54Z", false, null))
        dazzOff.monitor(DataEntry("computer/hostname1", "1970-01-01T00:03:54Z", true,
            """ { "dazz-off": true } """))
        dazzOff.monitor(DataEntry("computer/hostname2", "1970-01-01T00:03:54Z", true,
            """ { "dazz-off": true, "ip": "192.168.0.0" } """))

        clock.add(2 * 24 * 3600 * 1000L)
        hostnameToAddress["hostname1"  ] = mock<InetAddress>()
        hostnameToAddress["192.168.0.0"] = mock<InetAddress>()
        addressToPing[ hostnameToAddress["hostname1"  ]!! ] = true
        addressToPing[ hostnameToAddress["192.168.0.0"]!! ] = true

        dazzOff.periodicCheck()

        assertThat(resolvedHostnames).containsExactly(
            "hostname1",   // 1 retry (immediate success)
            "192.168.0.0", // 1 retry (immediate success)
        ).inOrder()

        verifyNoInteractions(mockStore)

        assertThat(logger.string).doesNotContain("DazzOff: Detected 'hostname1' is off")
        assertThat(logger.string).doesNotContain("DazzOff: Detected 'hostname2' is off")
    }
}
