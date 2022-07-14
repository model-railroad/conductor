package com.alfray.conductor.v2.simulator

import com.alflabs.conductor.jmri.FakeJmriProvider
import com.alflabs.conductor.jmri.IJmriSensor
import com.alflabs.conductor.jmri.IJmriThrottle
import com.alflabs.conductor.jmri.IJmriTurnout
import com.alflabs.utils.IClock
import com.alflabs.utils.ILogger
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SimulJmriProvider @Inject constructor(
    val clock: IClock
) : FakeJmriProvider() {
    private val TAG = javaClass.simpleName

    fun clear() {
        mSensors.clear()
        mThrottles.clear()
        mTurnouts.clear()
    }

    override fun getSensor(systemName: String?): IJmriSensor? {
        if (systemName == null) return null
        return mSensors.computeIfAbsent(systemName) { name -> SimulSensor(name) }
    }

    override fun getThrottle(dccAddress: Int): IJmriThrottle {
        val logger: ILogger = this
        return mThrottles.computeIfAbsent(dccAddress) { address -> SimulThrottle(address, logger) }
    }

    override fun getTurnout(systemName: String?): IJmriTurnout? {
        val logger: ILogger = this
        if (systemName == null) return null
        return mTurnouts.computeIfAbsent(systemName) { name -> SimulTurnout(name, logger) }
    }
}

