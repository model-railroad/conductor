package com.alflabs.conductor.v2.ui

import com.alflabs.conductor.v2.IActivableDisplayAdapter
import com.alflabs.conductor.v2.ISensorDisplayAdapter
import com.alflabs.conductor.v2.IThrottleDisplayAdapter
import java.net.URI

class StatusWindow3 : IStatusWindow {
    override fun open(windowCallback: IWindowCallback) {
        TODO("Not yet implemented")
    }

    override fun updateScriptName(scriptName: String) {
        TODO("Not yet implemented")
    }

    override fun setSimulationMode(isSimulation: Boolean) {
        TODO("Not yet implemented")
    }

    override fun enterKioskMode() {
        TODO("Not yet implemented")
    }

    override fun displaySvgMap(svgDocument: String?, mapUrl: URI) {
        TODO("Not yet implemented")
    }

    override fun updateUI() {
        TODO("Not yet implemented")
    }

    override fun updateMainLog(logText: String) {
        TODO("Not yet implemented")
    }

    override fun updateSimuLog(logText: String) {
        TODO("Not yet implemented")
    }

    override fun updatePause(isPaused: Boolean) {
        TODO("Not yet implemented")
    }

    override fun clearUpdates() {
        TODO("Not yet implemented")
    }

    override fun registerThrottles(throttles: MutableList<IThrottleDisplayAdapter>) {
        TODO("Not yet implemented")
    }

    override fun registerActivables(
        sensors: MutableList<ISensorDisplayAdapter>,
        blocks: MutableList<IActivableDisplayAdapter>,
        turnouts: MutableList<IActivableDisplayAdapter>
    ) {
        TODO("Not yet implemented")
    }

}
