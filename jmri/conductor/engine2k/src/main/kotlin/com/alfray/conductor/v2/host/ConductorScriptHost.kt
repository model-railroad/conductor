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

package com.alfray.conductor.v2.host

import com.alfray.conductor.v2.dagger.Script2kScope
import com.alfray.conductor.v2.script.ConductorScript
import com.alfray.conductor.v2.script.dsl.IConductor
import javax.inject.Inject
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.api.constructorArgs
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

@Script2kScope
internal class ConductorScriptHost @Inject constructor() {
    private val scriptingHost = BasicJvmScriptingHost()

    fun eval(source: SourceCode, conductor: IConductor) : ResultWithDiagnostics<EvaluationResult> =
        scriptingHost.evalWithTemplate<ConductorScript>(
            source,
            evaluation = {
                constructorArgs(conductor)
            }
        )
}
