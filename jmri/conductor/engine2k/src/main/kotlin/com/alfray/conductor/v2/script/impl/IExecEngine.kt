package com.alfray.conductor.v2.script.impl

interface IExecEngine {
    /** Initializes state before executing the script. */
    fun onExecStart()

    /** Handles one execution of events. */
    fun onExecHandle()
}
