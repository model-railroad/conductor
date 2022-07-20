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

import com.alfray.conductor.v2.Script2kLoader
import com.alfray.conductor.v2.script.impl.CondCache
import dagger.Subcomponent

@Script2kScope
@Subcomponent
interface IScript2kComponent {
    val script2kLoader: Script2kLoader
    val condCache: CondCache

    @Subcomponent.Factory
    interface Factory {
        fun createComponent(): IScript2kComponent
    }
}
