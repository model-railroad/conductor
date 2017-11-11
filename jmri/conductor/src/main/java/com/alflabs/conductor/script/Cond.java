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

package com.alflabs.conductor.script;

/**
 * Represents one event condition, which is composed of a conditional and can be negated.
 */
class Cond {
    private final IConditional mConditional;
    private final boolean mNegated;

    Cond(IConditional conditional, boolean negated) {
        mConditional = conditional;
        mNegated = negated;
    }

    boolean eval(CondCache cache) {
        Boolean cached = cache.get(mConditional);
        boolean status;
        if (cached != null) {
            status = cached;
        } else {
            status = mConditional.isActive();
            cache.put(mConditional, status);
        }

        if (mNegated) {
            status = !status;
        }
        return status;
    }
}
