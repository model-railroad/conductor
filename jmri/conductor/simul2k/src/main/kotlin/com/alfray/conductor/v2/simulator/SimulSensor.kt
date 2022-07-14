package com.alfray.conductor.v2.simulator

import com.alflabs.conductor.jmri.IJmriSensor
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

/** Creates a new sensor for the given JMRI system name. */
@AssistedFactory
interface ISimulSensorFactory {
    fun create(systemName: String) : SimulSensor
}

class SimulSensor @AssistedInject constructor(
    @Assisted val systemName: String
) : IJmriSensor {
    private var _active : Boolean = false

    override fun isActive(): Boolean {
        return _active
    }

    override fun setActive(active: Boolean) {
        _active = active
    }
}
