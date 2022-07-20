/*
 * Project: Conductor
 * Copyright (C) 2022 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alfray.conductor.v2.dagger

import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A global singleton context for the currently running script.
 * It holds the current script filename, the script-scoped component, and the loading error.
 */
//@Singleton
class Script2kContext
//@Inject
constructor(private val script2kCompFactory: IScript2kComponent.Factory) {
    var script2kComponent: Optional<IScript2kComponent> = Optional.empty()
        private set

    fun createComponent() : IScript2kComponent {
        check(!script2kComponent.isPresent)
        val scriptComponent = script2kCompFactory.createComponent()
        script2kComponent = Optional.of(scriptComponent)
        return scriptComponent
    }

    fun reset() {
        script2kComponent = Optional.empty()
    }
}
