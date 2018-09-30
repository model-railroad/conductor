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

import com.alflabs.conductor.util.Analytics;
import com.alflabs.conductor.util.EventLogger;
import com.alflabs.conductor.util.ILocalDateTimeNowProvider;
import com.alflabs.conductor.util.Logger;
import com.alflabs.kv.KeyValueServer;
import com.alflabs.utils.FileOps;
import com.alflabs.utils.IClock;
import com.alflabs.utils.ILogger;
import com.alflabs.utils.JavaClock;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Module
public class ConductorModule {
    private final IJmriProvider mJmriProvider;

    public ConductorModule(IJmriProvider jmriProvider) {
        mJmriProvider = jmriProvider;
    }

    @Singleton
    @Provides
    public IClock provideClock() {
        return new JavaClock();
    }

    @Singleton
    @Provides
    public ILocalDateTimeNowProvider provideLocalDateTime() {
        return LocalDateTime::now;
    }

    @Singleton
    @Provides
    public IJmriProvider provideJmriProvider() {
        return mJmriProvider;
    }

    @Singleton
    @Provides
    public Logger provideLogger() {
        return mJmriProvider;
    }

    @Singleton
    @Provides
    public KeyValueServer provideKeyValueServer(ILogger logger) {
        return new KeyValueServer(logger);
    }

    @Singleton
    @Provides
    public FileOps provideFileOps() {
        return new FileOps();
    }

    @Singleton
    @Provides
    public Analytics provideAnalytics(ILogger logger, FileOps fileOps, KeyValueServer keyValue) {
        return new Analytics(logger, fileOps, keyValue);
    }

    @Singleton
    @Provides
    public EventLogger provideEventLogger(ILogger logger, FileOps fileOps, ILocalDateTimeNowProvider localDateTimeNow) {
        return new EventLogger(logger, fileOps, localDateTimeNow);
    }

    @Singleton
    @Provides
    public ILogger provideILogger(Logger logger) {
        return new ILogger() {
            @Override
            public void d(String tag, String message) {
                logger.log(tag + ": " + message);
            }

            @Override
            public void d(String tag, String message, Throwable tr) {
                logger.log(tag + ": " + message + ": " + tr);
            }
        };
    }
}
