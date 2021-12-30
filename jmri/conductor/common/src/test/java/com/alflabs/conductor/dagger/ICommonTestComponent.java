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
import com.alflabs.conductor.util.Analytics;
import com.alflabs.conductor.util.EventLogger;
import com.alflabs.conductor.util.FakeClockModule;
import com.alflabs.conductor.util.FakeFileOpsModule;
import com.alflabs.conductor.util.JsonSender;
import com.alflabs.kv.IKeyValue;
import com.alflabs.utils.FakeClock;
import com.alflabs.utils.FakeFileOps;
import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
        ExecutorModule.class,
        FakeClockModule.class,
        FakeFileOpsModule.class,
        FakeKeyValueModule.class,
        LoggerModule.class,
        MockAnalyticsModule.class,
        MockEventLoggerModule.class,
        MockHttpClientModule.class,
        MockJsonSenderModule.class,
        MockRandomModule.class,
        })
public interface ICommonTestComponent extends ICommonComponent {

    FakeClock getFakeClock();
    FakeFileOps getFakeFileOps();

    IKeyValue getKeyValue();
    Analytics getAnalytics();
    JsonSender getJsonSender();
    EventLogger getEventLogger();

    @Component.Factory
    interface Factory {
        ICommonTestComponent createComponent(@BindsInstance IJmriProvider jmriProvider);
    }
}
