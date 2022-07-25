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

import com.alfray.conductor.v2.script.ExecContext
import com.alfray.conductor.v2.script.dsl.IActiveRoute
import com.alfray.conductor.v2.script.dsl.INode
import com.alfray.conductor.v2.script.dsl.IRoute
import com.alfray.conductor.v2.script.dsl.TAction

/**
 */
internal abstract class RouteBase(
    override val owner: IActiveRoute,
    builder: RouteBaseBuilder
) : IRoute {
    protected val actionOnActivate = builder.actionOnActivate
    protected val actionOnRecover = builder.actionOnRecover
    protected var callOnActivate: TAction? = null
    protected var callOnRecover: TAction? = null
    protected val context = ExecContext(ExecContext.State.ROUTE)
    var error = false
        protected set(v) {
            field = v
            (owner as ActiveRoute).reportError(this, v)
        }

    protected inline fun assertOrError(value: Boolean, lazyMessage: () -> Any) {
        if (!value) {
            error = true
            val message = lazyMessage()
            throw IllegalStateException(message.toString())
        }
    }

    override fun activate() {
        owner.activate(this)
        callOnActivate = actionOnActivate
    }

    abstract override fun start_node(node: INode)
}


