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

import com.alflabs.conductor.util.EventLogger
import com.alflabs.utils.IClock
import com.alflabs.utils.ILogger
import com.alfray.conductor.v2.script.dsl.Delay
import com.alfray.conductor.v2.script.dsl.ITimer
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

internal class Timer @AssistedInject constructor(
    private val clock: IClock,
    private val logger: ILogger,
    private val eventLogger: EventLogger,
    @Assisted override val delay: Delay
) : ITimer {
    private val TAG = javaClass.simpleName
    override val name = "@timer@${delay.seconds}"
    private var endTS: Long = 0L
    private var activated = false

    override val active: Boolean
        get() {
            if (!activated) {
                activated = endTS != 0L && now() >= endTS
                if (activated) {
                    eventLogger.logAsync(EventLogger.Type.Timer, name, "activated")
                }
            }
            return activated
        }

    override fun not(): Boolean = !active

    override fun start() {
        if (endTS == 0L) {
            endTS = now() + delay.seconds * 1000
            activated = false
            eventLogger.logAsync(
                EventLogger.Type.Timer, name,
                "start:${delay.seconds}"
            )
        } else {
            logger.d(
                TAG,
                "Warning: ignoring ongoing Timer start for $name"
            )
        }
    }

    override fun reset() {
        endTS = 0L
        activated = false
        eventLogger.logAsync(EventLogger.Type.Timer, name, "reset")
    }

    private fun now(): Long = clock.elapsedRealtime()
}
