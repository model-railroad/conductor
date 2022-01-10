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

import com.alflabs.annotations.Null;
import com.alflabs.conductor.IEntryPoint;
import com.alflabs.conductor.jmri.IJmriProvider;
import com.alflabs.conductor.v2.ui.StatusWindow2;

public class EntryPoint2 implements IEntryPoint {
    private static final String TAG = EntryPoint2.class.getSimpleName();

    public void init(@Null String simulationScript) {
        StatusWindow2 sw2 = new StatusWindow2();
        sw2.open();
    }

    @Override
    public boolean setup(IJmriProvider jmriProvider, String scriptPath) {
        return false;
    }

    @Override
    public boolean handle() {
        return false;
    }

    public void runDevLoop() {
    }
}
