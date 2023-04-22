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

package com.alfray.conductor.v2.script

import com.alfray.conductor.v2.script.dsl.TAction

/**
 * An action to be executed in a given context.
 *
 * An on-rule action has two contextes:
 * ` <top level script context>
 *   on { condition } then { <inner on-rule context> action } `
 * - The "owner" context is where the on-rule was created, e.g. the top-level script.
 *   This is used to cache actions to avoid repeating them twice.
 * - The "invoke" context is the context in which the inner action is to be executed.
 *   In this case an on-rule creates its own inner context.
 *
 * Inner structures (route callbacks, node callbacks) create their own context, in which
 * case the owner context and the invoke context is the same. These are the cases where we
 * cannot have any on-rules.
 */
internal data class ExecAction(
    /** The context where the action was created and is cached. */
    val ownerContext: ExecContext,
    /** The context in which the action is to be invoked. */
    val invokeContext: ExecContext,
    /** The action to invoke. */
    val action: TAction)
