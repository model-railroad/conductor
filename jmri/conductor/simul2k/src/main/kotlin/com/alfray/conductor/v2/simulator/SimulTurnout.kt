package com.alfray.conductor.v2.simulator

import com.alflabs.conductor.jmri.IJmriTurnout
import com.alflabs.utils.ILogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

/** Creates a new turnout for the given JMRI system name. */
@AssistedFactory
interface ISimulTurnoutFactory {
    fun create(systemName: String) : SimulTurnout
}

class SimulTurnout @AssistedInject constructor(
    private val logger: ILogger,
    @Assisted val systemName: String
) : IJmriTurnout {
    private val TAG = javaClass.simpleName
    private var _normal = true

    override fun isNormal(): Boolean {
        return _normal
    }

    override fun setTurnout(normal: Boolean) {
        _normal = normal
        logger.d(TAG, String.format("[%s] Turnout: %s", systemName, if (normal) "Normal" else "Reverse"))
    }
}
