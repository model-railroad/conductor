/*
 * Project: Conductor
 * Copyright (C) 2017 alf.labs gmail com,
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

package com.alflabs.conductor.jmri;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.utils.ILogger;

/**
 * Interface to JMRI to provide JMRI adapter objects.
 */
public interface IJmriProvider extends ILogger {
    /**
     * Returns a new JMRI throttle adapter for the given DCC long address.
     * Returns null if the object cannot be created.
     */
    @Null
    IJmriThrottle getThrottle(int dccAddress);

    /**
     * Returns a new JMRI sensor adapter for the given sensor system name.
     * Returns null if the object cannot be created.
     */
    @Null
    IJmriSensor getSensor(@NonNull String systemName);

    /**
     * Returns a new JMRI turnout adapter for the given turnout system name.
     * Returns null if the object cannot be created.
     */
    @Null
    IJmriTurnout getTurnout(@NonNull String systemName);
}
