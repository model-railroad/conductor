package com.alfray.conductor.v2.simulator

import com.alflabs.conductor.jmri.FakeJmriProvider
import com.alflabs.conductor.jmri.IJmriProvider

class Simul2kEntryPoint {
    val jmriProvider: IJmriProvider = FakeJmriProvider()

    fun placeholder(): Unit {
        println("blah")
    }
}
