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

package com.alflabs.conductor;

/** Abstraction of a JMRI sensor. */
public interface IJmriSensor {
    /** True when the sensor is valid and active. */
    boolean isActive();
    /**
     * Overrides the state of the JMRI sensor.
     * This works when using the DevelopmentEntryPoint and may not do anything when
     * running with a real JMRI instance (the sensor state will be reset when the
     * sensor is updated by JMRI).
     */
    void setActive(boolean active);
}
