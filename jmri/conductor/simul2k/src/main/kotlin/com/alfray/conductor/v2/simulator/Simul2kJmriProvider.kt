package com.alfray.conductor.v2.simulator

import com.alflabs.conductor.jmri.FakeJmriProvider
import com.alflabs.conductor.jmri.IJmriProvider
import com.alflabs.utils.IClock
import com.alflabs.utils.ILogger
import javax.inject.Inject


class Simul2kJmriProvider @Inject constructor(
    val clock: IClock
) {
    private companion object {
        val TAG = Simul2kJmriProvider::class.java.simpleName
    }
    private val jmriProvider: IJmriProvider = FakeJmriProvider()
    private val logger: ILogger = jmriProvider

    fun jmriProvider() : IJmriProvider {
        logger.d(TAG, "Simul2k providing IJmriProvider at " + clock.elapsedRealtime())
        return jmriProvider
    }
}
