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
import com.alfray.conductor.v2.script.dsl.IActiveRoute
import com.alfray.conductor.v2.script.dsl.INode
import com.alfray.conductor.v2.script.dsl.IRouteIdle

internal class RouteIdle(
    owner: IActiveRoute,
    builder: RouteIdleBuilder
) : RouteBase(owner, builder), IRouteIdle {

    override fun start_node(node: INode) {
        assertOrError(false) {
            "No start_node to set in an idle route."
        }
    }

    /** Invoked by the ExecEngine2 loop to collect all actions to evaluate. */
    override fun collectActions(execActions: MutableList<ExecAction>) {
        // no-op
    }
}
