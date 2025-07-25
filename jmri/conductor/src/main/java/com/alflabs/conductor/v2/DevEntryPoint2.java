/*
 * Project: Conductor
 * Copyright (C) 2019 alf.labs gmail com,
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

package com.alflabs.conductor.v2;

/** Entry point controlled for development purposes using a fake no-op JMRI interface. */
public class DevEntryPoint2 {
    private static final String TAG = DevEntryPoint2.class.getSimpleName();

    public static void main(String[] args) {
        System.out.println(TAG + " - Start");
        EntryPoint2 entry = new EntryPoint2();

        entry.init("script_v34.01_bl191+ml9538+1072+tl6885.conductor.kts");
        entry.runDevLoop();
        System.out.println(TAG + " - End");
    }
}
