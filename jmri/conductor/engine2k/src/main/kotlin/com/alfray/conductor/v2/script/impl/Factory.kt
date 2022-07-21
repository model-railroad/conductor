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

import com.alflabs.conductor.jmri.IJmriProvider
import com.alflabs.conductor.util.EventLogger
import com.alflabs.kv.IKeyValue
import com.alflabs.utils.IClock
import com.alflabs.utils.ILogger
import com.alfray.conductor.v2.dagger.Script2kScope
import com.alfray.conductor.v2.script.EStopHandler
import javax.inject.Inject

@Script2kScope
internal class Factory @Inject constructor(
    private val clock: IClock,
    private val logger: ILogger,
    private val keyValue: IKeyValue,
    private val eventLogger: EventLogger,
    private val jmriProvider: IJmriProvider,
    private val eStopHandler: EStopHandler,
) {
    internal fun createSensor(systemName: String) : Sensor =
        Sensor(keyValue, eventLogger, jmriProvider, systemName)

    internal fun createBlock(systemName: String) : Block =
        Block(keyValue, eventLogger, jmriProvider, systemName)

    internal fun createTurnout(systemName: String) : Turnout =
        Turnout(keyValue, jmriProvider, systemName)

    internal fun createThrottle(dccAddress: Int) : Throttle =
        Throttle(clock, logger, keyValue, eventLogger, jmriProvider, dccAddress)
}

