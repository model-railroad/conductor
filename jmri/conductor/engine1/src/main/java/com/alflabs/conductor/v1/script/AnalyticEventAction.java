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

package com.alflabs.conductor.v1.script;

import com.alflabs.annotations.NonNull;
import com.alflabs.conductor.util.Analytics;

import java.util.Map;

public class AnalyticEventAction implements IAction {

    public static final String CATEGORY = "category";
    public static final String ACTION = "action";
    public static final String LABEL = "label";
    public static final String USER = "user";

    private final Analytics mAnalytics;
    private final Map<String, String> mArguments;
    private final ValueResolver mLabelResolver;
    private final ValueResolver mUserResolver;

    public interface ValueResolver {
        @NonNull
        public String resolve(String varName);
    }

    public AnalyticEventAction(
            Analytics analytics,
            Map<String, String> arguments,
            ValueResolver labelResolver,
            ValueResolver userResolver) {

        mAnalytics = analytics;
        mArguments = arguments;
        mLabelResolver = labelResolver;
        mUserResolver = userResolver;
    }

    @Override
    public void execute() {
        mAnalytics.sendEvent(
                mArguments.get(CATEGORY),
                mArguments.get(ACTION),
                mLabelResolver.resolve(mArguments.get(LABEL)),
                mUserResolver.resolve(mArguments.get(USER))
        );
    }
}
