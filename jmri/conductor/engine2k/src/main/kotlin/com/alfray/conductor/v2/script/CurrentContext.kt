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
    val scriptLoaderContext = ExecContext(ExecContext.Reason.LOAD_SCRIPT)
    private var current: ExecContext? = null

    fun changeContext(context: ExecContext) {
        current = context
    }

    fun resetContext() {
        current = null
    }

    /** Asserts that the current context is not null and is not the unexpected reason
     * Return the non-null context. */
    fun assertNotHasReason(tag: String, unexpected: ExecContext.Reason, lazyMessage: () -> Any): ExecContext {
        logger.assertOrThrow(
            tag,
            current != null && current!!.reason != unexpected
        ) {
            "${lazyMessage.invoke()} (Was ${current?.reason?.name})"
        }
        return current!!
    }

    /** Asserts that the current context is not null and is the expected reason.
     * Return the non-null context. */
    fun assertHasReason(tag: String, expected: List<ExecContext.Reason>, lazyMessage: () -> Any): ExecContext {
        logger.assertOrThrow(
            tag,
            current != null && expected.contains(current!!.reason)
            ) {
            "${lazyMessage.invoke()} (Was ${current?.reason?.name})"
        }
        return current!!
    }
}
