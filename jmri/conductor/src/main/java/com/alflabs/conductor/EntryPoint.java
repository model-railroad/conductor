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

import com.alflabs.conductor.jmri.IJmriProvider;
import com.alflabs.conductor.v1.EntryPoint1;
import com.alflabs.conductor.v2.EntryPoint2;

/** Interface controlled by Conductor.py */
public class EntryPoint implements IEntryPoint2 {
    private static final String TAG = EntryPoint2.class.getSimpleName();

    private IEntryPoint mImplementation;

    /**
     * Invoked when the JMRI automation is being setup, from the Jython script.
     *
     * Version defaults to 1, or to a valid CONDUCTOR_VERSION environment value.
     *
     * @return True to start the automation, false if there's a problem and it should not start.
     */
    @Override
    public boolean setup(IJmriProvider jmriProvider, String scriptPath) {
        int version = 1;
        try {
            String vers = System.getenv("CONDUCTOR_VERSION");
            if (vers != null) {
                version = Integer.parseInt(vers);
            }
        } catch (SecurityException ignore) {}
        return setup(version, jmriProvider, scriptPath);
    }

    /**
     * Invoked when the JMRI automation is being setup, from the Jython script.
     *
     * Valid version numbers are either 1 or 2.
     *
     * @return True to start the automation, false if there's a problem and it should not start.
     */
    @Override
    public boolean setup(int version, IJmriProvider jmriProvider, String scriptPath) {
        if (mImplementation != null) {
            throw new IllegalStateException("Conductor.setup() has already been called");
        }
        switch (version) {
        case 1:
            mImplementation = new EntryPoint1();
            break;
        case 2:
            mImplementation = new EntryPoint2();
            break;
        default:
            throw new IllegalStateException("CONDUCTOR_VERSION should be 1 or 2.");
        }
        System.out.println("Conductor setup for v" + version);
        return mImplementation.setup(jmriProvider, scriptPath);
    }

    /**
     * Invoked repeatedly by the automation Jython handler if {@link #setup(IJmriProvider, String)}
     * returned true.
     *
     * @return Will keep being called as long as it returns true.
     */
    @Override
    public boolean handle() {
        return mImplementation.handle();
    }
}
