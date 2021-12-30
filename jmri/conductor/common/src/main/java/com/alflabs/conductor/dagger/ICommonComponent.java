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

package com.alflabs.conductor.dagger;

import com.alflabs.conductor.jmri.IJmriProvider;
import com.alflabs.conductor.util.ClockModule;
import com.alflabs.conductor.util.FileOpsModule;
import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
        AnalyticsModule.class,
        ClockModule.class,
        EventLoggerModule.class,
        ExecutorModule.class,
        FileOpsModule.class,
        HttpClientModule.class,
        KeyValueModule.class,
        JsonSenderModule.class,
        RandomModule.class,
        })
public interface ICommonComponent {

    // Design: Use a @Component.Factory instead of @Component.Builder
    // to add @BindsInstance singleton values into the component.
    // They do the same thing but the factory pattern is a bit easier to use as it
    // means creation cannot forget to bind any arguments.

    @Component.Factory
    interface Factory {
        ICommonComponent createComponent(@BindsInstance IJmriProvider jmriProvider);
    }
}
