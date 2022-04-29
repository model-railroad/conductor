package com.alfray.conductor.v2.dagger

import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A global singleton context for the currently running script.
 * It holds the current script filename, the script-scoped component, and the loading error.
 */
@Singleton
internal class Script2kContext
@Inject constructor(val script2kCompFactory: IScript2kComponent.Factory) {
    var script2kComponent: Optional<IScript2kComponent> = Optional.empty()
    var scriptFile: Optional<File> = Optional.empty()

    fun reset() {
        script2kComponent = Optional.empty()
    }
}
