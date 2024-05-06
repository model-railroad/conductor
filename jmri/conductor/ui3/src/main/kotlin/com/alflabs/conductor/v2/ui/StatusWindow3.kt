package com.alflabs.conductor.v2.ui

import com.alflabs.conductor.v2.IActivableDisplayAdapter
import com.alflabs.conductor.v2.ISensorDisplayAdapter
import com.alflabs.conductor.v2.IThrottleDisplayAdapter
import java.net.URI

class StatusWindow3 : IStatusWindow {
    private lateinit var windowCallback: IWindowCallback

    override fun open(windowCallback: IWindowCallback) {
        println("StatusWindow3 - Not yet implemented - open")
        this.windowCallback = windowCallback
    }

    override fun updateScriptName(scriptName: String) {
        println("StatusWindow3 - Not yet implemented - updateScriptName")
    }

    override fun setSimulationMode(isSimulation: Boolean) {
        println("StatusWindow3 - Not yet implemented - setSimulationMode")
        windowCallback.onQuit() // TEMP
    }

    override fun enterKioskMode() {
        println("StatusWindow3 - Not yet implemented - enterKioskMode")
    }

    override fun displaySvgMap(svgDocument: String?, mapUrl: URI) {
        println("StatusWindow3 - Not yet implemented - displaySvgMap")
        windowCallback.onWindowSvgLoaded() // TEMP
    }

    override fun updateUI() {
        println("StatusWindow3 - Not yet implemented - updateUI")
    }

    override fun updateMainLog(logText: String) {
        println("StatusWindow3 - Not yet implemented - updateMainLog")
    }

    override fun updateSimuLog(logText: String) {
        println("StatusWindow3 - Not yet implemented - updateSimuLog")
    }

    override fun updatePause(isPaused: Boolean) {
        println("StatusWindow3 - Not yet implemented - updatePause")
    }

    override fun clearUpdates() {
        println("StatusWindow3 - Not yet implemented - clearUpdates")
    }

    override fun registerThrottles(throttles: MutableList<IThrottleDisplayAdapter>) {
        println("StatusWindow3 - Not yet implemented - registerThrottles")
    }

    override fun registerActivables(
        sensors: MutableList<ISensorDisplayAdapter>,
        blocks: MutableList<IActivableDisplayAdapter>,
        turnouts: MutableList<IActivableDisplayAdapter>
    ) {
        println("StatusWindow3 - Not yet implemented - registerActivables")
    }

}
