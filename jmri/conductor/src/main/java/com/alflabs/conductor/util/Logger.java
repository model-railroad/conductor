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

package com.alflabs.conductor.util;

/**
 * Drastically simplified interface for console logging.
 * <p/>
 * This is useful for dumping generic statements, typically on the console, typically for
 * casual debugging.
 * <p/>
 * Some refactoring should be done later to get rid of this and use LibUtils's {@code ILogger}
 * everywhere; consequently new code should probably avoid using this.
 */
public interface Logger {
    void log(String msg);

}
