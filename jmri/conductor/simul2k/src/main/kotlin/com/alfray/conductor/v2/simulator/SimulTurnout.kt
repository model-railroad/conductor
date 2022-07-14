package com.alfray.conductor.v2.simulator

import com.alflabs.conductor.jmri.IJmriTurnout
import com.alflabs.utils.ILogger

class SimulTurnout(
    val systemName: String,
    val logger: ILogger
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
