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

import com.alflabs.utils.ILogger
import com.alfray.conductor.v2.dagger.Script2kScope
import com.alfray.conductor.v2.utils.assertOrThrow
import javax.inject.Inject

@Script2kScope
internal class CurrentContext @Inject internal constructor(
    private val logger: ILogger
) {
    val scriptLoaderContext = ExecContext(ExecContext.State.LOAD_SCRIPT)
    private var current: ExecContext? = null

    fun changeContext(context: ExecContext) {
        current = context
    }

    fun resetContext() {
        current = null
    }

    /** Asserts that the current context is not null and __is not__ the script-loader context.
     * Return the non-null context. */
    fun assertNotInScriptLoader(tag: String, lazyMessage: () -> Any): ExecContext {
        logger.assertOrThrow(
            tag,
            current != null && current !== scriptLoaderContext,
            lazyMessage)
        return current!!
    }

    /** Asserts that the current context is not null and __is__ the script-loader context.
     * Return the non-null context. */
    fun assertInScriptLoader(tag: String, lazyMessage: () -> Any): ExecContext {
        logger.assertOrThrow(
            tag,
            current != null && current === scriptLoaderContext,
            lazyMessage)
        return current!!
    }
}
