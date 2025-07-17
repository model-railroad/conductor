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

import com.alfray.conductor.v2.script.CondCacheTest
import com.alfray.conductor.v2.script.impl.TimerTest
import com.alfray.conductor.v2.script.ScriptDslTest2k
import com.alfray.conductor.v2.script.ScriptTest2Test2k
import com.alfray.conductor.v2.script.ScriptTest3Test2k
import com.alfray.conductor.v2.script.impl.ThrottleTest
import com.alfray.conductor.v2.script.impl.RouteGraphTest
import com.alfray.conductor.v2.script.dsl.ExportedVarsTest
import com.alfray.conductor.v2.script.ValidateScriptsSyntax2k
import com.alfray.conductor.v2.script.impl.SequenceRouteManagerTest
import dagger.Subcomponent

@Script2kScope
@Subcomponent
interface IScript2kTestComponent: IScript2kComponent {
    fun inject(timerTest: TimerTest)
    fun inject(scriptDslTest2K: ScriptDslTest2k)
    fun inject(throttleTest: ThrottleTest)
    fun inject(condCacheTest: CondCacheTest)
    fun inject(routeGraphTest: RouteGraphTest)
    fun inject(exportedVarsTest: ExportedVarsTest)
    fun inject(scriptTest2Test2K: ScriptTest2Test2k)
    fun inject(scriptTest3Test2K: ScriptTest3Test2k)
    fun inject(validateScriptsSyntax2K: ValidateScriptsSyntax2k)
    fun inject(sequenceRouteManagerTest: SequenceRouteManagerTest)

    @Subcomponent.Factory
    interface Factory {
        fun createTestComponent(): IScript2kTestComponent
    }
}
