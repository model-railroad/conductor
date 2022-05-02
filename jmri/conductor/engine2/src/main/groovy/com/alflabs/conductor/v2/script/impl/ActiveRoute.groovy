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

package com.alflabs.conductor.v2.script.impl

import com.alflabs.annotations.NonNull

class ActiveRoute extends BaseVar {
    private final ActiveRouteInfo mInfo
    private Route mActiveRoute

    ActiveRoute(@NonNull ActiveRouteInfo info) {
        this.mInfo = info
        this.mActiveRoute = null
    }

    @NonNull
    Route[] getRoutes() {
        return mInfo.mRoutes
    }

    @NonNull
    List<IEvalRule> evaluateRules() {
        if (mActiveRoute != null) {
            return mActiveRoute.evaluateRules()
        }
        return Collections.emptyList()
    }
}
