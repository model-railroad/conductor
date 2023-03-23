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

package com.alfray.conductor.v2.simulator

import com.alflabs.conductor.jmri.FakeJmriProvider
import com.alflabs.conductor.jmri.IJmriSensor
import com.alflabs.conductor.jmri.IJmriThrottle
import com.alflabs.conductor.jmri.IJmriTurnout
import com.alflabs.utils.IClock
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SimulJmriProvider @Inject constructor(
    private val clock: IClock,
    private val simulSensorFactory: ISimulSensorFactory,
    private val simulThrottleFactory: ISimulThrottleFactory,
    private val simulTurnoutFactory: ISimulTurnoutFactory,
) : FakeJmriProvider(), IExecSimul, ISimulCallback {
    private val TAG = javaClass.simpleName

    fun clear() {
        mSensors.clear()
        mThrottles.clear()
        mTurnouts.clear()
    }

    override fun getSensor(systemName: String?): IJmriSensor? {
        if (systemName == null) return null
        return mSensors.computeIfAbsent(systemName) { name -> simulSensorFactory.create(name) }
    }

    override fun getThrottle(dccAddress: Int): IJmriThrottle {
        return mThrottles.computeIfAbsent(dccAddress) { address -> simulThrottleFactory.create(address) }
    }

    override fun getTurnout(systemName: String?): IJmriTurnout? {
        if (systemName == null) return null
        return mTurnouts.computeIfAbsent(systemName) { name -> simulTurnoutFactory.create(name) }
    }

    override fun onExecStart() {
        mThrottles.values.forEach { (it as SimulThrottle).onExecStart() }
    }

    override fun onExecHandle() {
        mThrottles.values.forEach { (it as SimulThrottle).onExecHandle() }
    }

    /** Notifies the simulator that the sum of timers for that block have changed. */
    override fun onBlockTimersChanged(systemName: String, sumTimersSec: Int) {
        mThrottles.values
            .mapNotNull { (it as SimulThrottle).block }
            .filter { it.systemName == systemName }
            .forEach { it.updateNodeTimers(sumTimersSec) }
    }

    /**
     * Sets the route definition for a given DCC throttle.
     *
     * Once a throttle is active and has a graph, the simulated throttle updates the blocks' states.
     */
    override fun setRoute(dccAddress: Int, routeTimeout: Int, graph: SimulRouteGraph) {
        this.d(TAG, "[Throttle $dccAddress] Set route $graph")
        val t = getThrottle(dccAddress) as SimulThrottle
        t.setGraph(graph)
        t.routeTimeout = routeTimeout
    }

    fun getUiLogOutput(): String {
        return mThrottles.values.map { (it as SimulThrottle).getUiLogOutput() }.joinToString("\n")
    }
}

