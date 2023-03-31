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

package com.alfray.conductor.v2.script.impl

import com.alfray.conductor.v2.script.ExecAction
import com.alfray.conductor.v2.script.ExecContext
import com.alfray.conductor.v2.script.dsl.IActive
import com.alfray.conductor.v2.script.dsl.IOnRule
import com.alfray.conductor.v2.script.dsl.TAction
import com.alfray.conductor.v2.script.dsl.TCondition
import com.alfray.conductor.v2.utils.ConductorExecException

internal open class OnRule(private val condition: TCondition) : IOnRule {
    private var action: TAction? = null
    private val context = ExecContext(ExecContext.Reason.ON_RULE)

    override fun then(action: TAction) {
        this.action = action
    }

    open fun evaluateCondition() : Boolean {
        val result = condition.invoke()
        val cond : Boolean =
            when (result) {
                is Boolean -> result
                is IActive -> result.active
                else -> throw ConductorExecException("Invalid Condition Return type")
            }
        return cond
    }

    open fun getAction() : ExecAction {
        return action
            ?.let { ExecAction(context, it) }
            ?: throw ConductorExecException(
                "Undefined Rule Action ('on..then' statement missing the 'then' part).")
    }

    fun clearTimers() {
        context.clearTimers()
    }
}
