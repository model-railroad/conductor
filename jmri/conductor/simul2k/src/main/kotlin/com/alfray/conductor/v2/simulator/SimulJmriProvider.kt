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
) : FakeJmriProvider(), IExecSimul {
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

    fun getUiLogOutput(): String {
        return mThrottles.values.map { (it as SimulThrottle).getUiLogOutput() }.joinToString("\n")
    }
}

