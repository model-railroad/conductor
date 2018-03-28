/*
 * Project: Conductor
 * Copyright (C) 2018 alf.labs gmail com,
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

import com.alflabs.annotations.NonNull;
import com.alflabs.conductor.util.Analytics;

import java.util.Map;

public class AnalyticPageAction implements IAction {

    public static final String PATH = "path";
    public static final String URL = "url";
    public static final String USER = AnalyticEventAction.USER;

    private final Analytics mAnalytics;
    private final Map<String, String> mArguments;
    private final AnalyticEventAction.ValueResolver mUrlResolver;
    private final AnalyticEventAction.ValueResolver mPathResolver;
    private final AnalyticEventAction.ValueResolver mUserResolver;

    public AnalyticPageAction(
            Analytics analytics,
            Map<String, String> arguments,
            AnalyticEventAction.ValueResolver urlResolver,
            AnalyticEventAction.ValueResolver pathResolver,
            AnalyticEventAction.ValueResolver userResolver) {

        mAnalytics = analytics;
        mArguments = arguments;
        mUrlResolver = urlResolver;
        mPathResolver = pathResolver;
        mUserResolver = userResolver;
    }

    @Override
    public void execute() {
        mAnalytics.sendPage(
                mUrlResolver.resolve(mArguments.get(URL)),
                mPathResolver.resolve(mArguments.get(PATH)),
                mUserResolver.resolve(mArguments.get(USER))
        );
    }
}
