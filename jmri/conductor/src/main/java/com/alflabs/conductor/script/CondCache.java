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

import java.util.HashMap;
import java.util.Map;

class CondCache {
    private final Map<IConditional, Boolean> mCache = new HashMap<>();

    void clear() {
        mCache.clear();
    }

    void put(IConditional conditional, boolean status) {
        mCache.put(conditional, status);
    }

    Boolean get(IConditional conditional) {
        return mCache.get(conditional);
    }
}
