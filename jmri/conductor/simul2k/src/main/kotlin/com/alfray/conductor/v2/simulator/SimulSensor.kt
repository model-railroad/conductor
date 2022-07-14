package com.alfray.conductor.v2.simulator

import com.alflabs.conductor.jmri.IJmriSensor

class SimulSensor(val systemName: String) : IJmriSensor {
    private var _active : Boolean = false

    override fun isActive(): Boolean {
        return _active
    }

    override fun setActive(active: Boolean) {
        _active = active
    }
}
