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
import com.alflabs.conductor.util.Analytics;
import com.alflabs.conductor.util.EventLogger;
import com.alflabs.conductor.util.ILocalDateTimeNowProvider;
import com.alflabs.conductor.util.JsonSender;
import com.alflabs.kv.KeyValueServer;
import com.alflabs.utils.FileOps;
import com.alflabs.utils.IClock;
import com.alflabs.utils.ILogger;
import com.alflabs.utils.JavaClock;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

import javax.inject.Named;
import javax.inject.Singleton;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
    public ILogger provideLogger() {
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
    public OkHttpClient provideOkHttpClient() {
        return new OkHttpClient();
    }

    @Singleton
    @Provides
    public Random provideRandom() {
        return new Random();
    }

    @Singleton
    @Provides
    @Named("JsonDateFormat")
    public DateFormat provideJsonDateFormat() {
        // Format timestamps using ISO 8601
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df;
    }

    @Singleton
    @Provides
    @Named("SingleThreadExecutor")
    public ScheduledExecutorService provideScheduledExecutorService() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    @Singleton
    @Provides
    public Analytics provideAnalytics(
            ILogger logger,
            IClock clock,
            FileOps fileOps,
            KeyValueServer keyValue,
            OkHttpClient okHttpClient,
            Random random,
            @Named("SingleThreadExecutor") ScheduledExecutorService executor) {
        return new Analytics(logger, clock, fileOps, keyValue, okHttpClient, random, executor);
    }

    @Singleton
    @Provides
    public JsonSender provideJsonSender(ILogger logger,
                                        FileOps fileOps,
                                        IClock clock,
                                        OkHttpClient okHttpClient,
                                        @Named("JsonDateFormat") DateFormat jsonDateFormat,
                                        @Named("SingleThreadExecutor") ScheduledExecutorService executor) {
        return new JsonSender(logger, fileOps, clock, okHttpClient, jsonDateFormat, executor);
    }

    @Singleton
    @Provides
    public EventLogger provideEventLogger(ILogger logger, FileOps fileOps, ILocalDateTimeNowProvider localDateTimeNow) {
        return new EventLogger(logger, fileOps, localDateTimeNow);
    }
}
