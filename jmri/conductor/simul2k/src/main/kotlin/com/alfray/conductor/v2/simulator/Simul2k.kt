package com.alfray.conductor.v2.simulator

import com.alflabs.utils.ILogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Simul2k @Inject constructor(
    val jmriProvider: SimulJmriProvider
) : IExecSimul by jmriProvider, ISimulCallback by jmriProvider {
    private val TAG = javaClass.simpleName
    private val logger: ILogger = jmriProvider

    /** The script is about to be loaded or reloaded. Clear any internal references. */
    fun onReload() {
        jmriProvider.clear()
    }

    fun getUiLogOutput(): String = jmriProvider.getUiLogOutput()
}
