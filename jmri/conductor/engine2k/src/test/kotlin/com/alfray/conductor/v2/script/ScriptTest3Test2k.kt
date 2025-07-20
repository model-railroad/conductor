/*
 * Project: Conductor
 * Copyright (C) 2022 alf.labs gmail com,
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

package com.alfray.conductor.v2.script

import com.alflabs.conductor.dagger.FakeEventLogger
import com.alflabs.kv.IKeyValue
import com.alflabs.utils.FakeClock
import com.alfray.conductor.v2.script.impl.SequenceRoute
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

/** Tests using script_test3.conductor.kts file. */
class ScriptTest3Test2k : ScriptTest2kBase() {
    @Inject lateinit var clock: FakeClock
    @Inject lateinit var keyValue: IKeyValue
    @Inject lateinit var eventLogger: FakeEventLogger

    @Before
    fun setUp() {
        createComponent()
        scriptComponent.inject(this)
        fileOps.writeBytes(
            "<svg/>".toByteArray(Charsets.UTF_8),
            fileOps.toFile("v2", "script", "Map 1.svg"))
    }

    @Test
    fun testScript3() {
        fileOps.writeBytes(
            "<svg/>".toByteArray(Charsets.UTF_8),
            fileOps.toFile("v2", "script", "src", "test", "resources", "v2", "Conductor Map Mainline 1.svg"))
        loadScriptFromFile("script_test3")
        assertResultNoError()

        assertThat(eventLogger.eventLogGetAndClear()).isEqualTo("")

//        assertThat(conductorImpl.blocks.keys).containsExactly(
//            "NS752", "NS753", "NS754", "NS755", "NS765", "NS768", "NS769", "NS770", "NS771", "NS773", "NS774", "NS775", "NS776", "NS786", "NS787")
//        assertThat(conductorImpl.sensors.keys).containsExactly(
//            "NS797", "NS828", "NS829")
//        assertThat(conductorImpl.turnouts.keys).containsExactly(
//            "NT311", "NT320", "NT321", "NT322", "NT324", "NT326", "NT330", "NT370", "NT504")
//        assertThat(conductorImpl.throttles.keys).containsExactly(
//            8749, 1072, 191)
//
//        assertThat(conductorImpl.routesContainers).hasSize(2)
//
//        // --- PA route ---
//        val pa = conductorImpl.routesContainers[0]
//        assertThat(pa.toString()).isEqualTo("RoutesContainer Mainline")
//
//        assertThat(pa.routes).hasSize(3)
//        assertThat(pa.routes[0].toString()).isEqualTo("Idle Mainline #0")
//        assertThat(pa.routes[1].toString()).isEqualTo("Sequence Mainline #1 (8749)")
//        assertThat(pa.routes[2].toString()).isEqualTo("Sequence Mainline #2 (1072)")
//
//        val paRoute1 = pa.routes[1] as SequenceRoute
//        assertThat(paRoute1.graph.toString()).isEqualTo(
//            "[{B503b}=>>{B503a}=>>{B321}=>>{B330}=>>{B340}=>>{B360}=>><B370>=<>{B360}=<>{B340}=<>{B330}=<>{B321}=<>{B503a}=<>{B503b}]")
//
//        val paRoute2 = pa.routes[2] as SequenceRoute
//        assertThat(paRoute2.graph.toString()).isEqualTo(
//            "[{B311}=>><B321>=<>{B311}],[<B321>->><B330>-<>{B321}-<>{B311}]")
//
//        // --- BL route ---
//        val bl = conductorImpl.routesContainers[1]
//        assertThat(bl.toString()).isEqualTo("RoutesContainer Branchline")
//
//        assertThat(bl.routes).hasSize(2)
//        assertThat(bl.routes[0].toString()).isEqualTo("Idle Branchline #0")
//        assertThat(bl.routes[1].toString()).isEqualTo("Sequence Branchline #1 (0191)")
//
//        val blRoute1 = bl.routes[1] as SequenceRoute
//        assertThat(blRoute1.graph.toString()).isEqualTo(
//            "[{BLParked}=>>{BLStation}=>>{BLTunnel}=>><BLReverse>=<>{BLTunnel}=<>{BLStation}=<>{BLParked}]")
//
//
//
//        // --- KV exports ---
//        execEngine.onExecHandle()
//        execEngine.onExecHandle()
//
//        val kv = keyValue.keys
//            .sorted()
//            .map { "$it=" + keyValue.getValue(it) }
//            .toList()
//        assertThat(kv).containsAtLeast(
//            "D/1072=0",
//            "D/191=0",
//            "D/8749=0",
//            "R/branchline\$counter=0",
//            "R/branchline\$status=Start",
//            "R/branchline\$throttle=0",
//            "R/branchline\$toggle=OFF",
//            "R/mainline\$counter=0",
//            "R/mainline\$status=Idle",
//            "R/mainline\$throttle=0",
//            "R/mainline\$toggle=OFF",
//            "R/routes={\"routeInfos\":[{\"name\":\"Mainline\",\"toggleKey\":\"R/mainline\$toggle\",\"statusKey\":\"R/mainline\$status\",\"counterKey\":\"R/mainline\$counter\",\"throttleKey\":\"R/mainline\$throttle\"},{\"name\":\"Branchline\",\"toggleKey\":\"R/branchline\$toggle\",\"statusKey\":\"R/branchline\$status\",\"counterKey\":\"R/branchline\$counter\",\"throttleKey\":\"R/branchline\$throttle\"}]}",
//            "S/NS752=OFF",
//            "S/NS753=OFF",
//            "S/NS754=OFF",
//            "S/NS755=OFF",
//            "S/NS765=OFF",
//            "S/NS768=OFF",
//            "S/NS769=OFF",
//            "S/NS770=OFF",
//            "S/NS771=OFF",
//            "S/NS773=OFF",
//            "S/NS774=OFF",
//            "S/NS775=OFF",
//            "S/NS776=OFF",
//            "S/NS786=OFF",
//            "S/NS787=OFF",
//            "S/NS797=OFF",
//            "S/NS828=OFF",
//            "S/NS829=OFF",
//            "T/NT311=N",
//            "T/NT320=N",
//            "T/NT321=N",
//            "T/NT322=N",
//            "T/NT324=N",
//            "T/NT326=N",
//            "T/NT330=N",
//            "T/NT370=N",
//            "T/NT504=N",
//            "V/rtac-psa-text={c:red}Automation Stopped",
//        )
    }
}
