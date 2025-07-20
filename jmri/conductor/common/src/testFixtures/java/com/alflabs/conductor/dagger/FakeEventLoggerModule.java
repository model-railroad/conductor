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

import com.alflabs.conductor.util.EventLogger;
import com.alflabs.conductor.util.ILocalDateTimeNowProvider;
import com.alflabs.utils.FileOps;
import com.alflabs.utils.IClock;
import com.alflabs.utils.ILogger;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/** Provides a dummy EventLogger that writes to the main ILogger. */
@Module
public abstract class FakeEventLoggerModule {
    @Singleton
    @Provides
    public static EventLogger provideEventLogger(IClock clock, ILogger logger, FileOps fileOps, ILocalDateTimeNowProvider localDateTimeNow) {
        FakeEventLogger e = new FakeEventLogger(clock, logger, fileOps, localDateTimeNow);
        return e;
    }

    @Singleton
    @Provides
    public static FakeEventLogger provideFakeEventLogger(EventLogger eventLogger) {
        return (FakeEventLogger) eventLogger;
    }
}
