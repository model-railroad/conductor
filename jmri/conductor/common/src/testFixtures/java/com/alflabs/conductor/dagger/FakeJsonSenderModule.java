/*
 * Project: Conductor
 * Copyright (C) 2025 alf.labs gmail com,
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

import com.alflabs.conductor.util.JsonSender;
import com.alflabs.utils.FileOps;
import com.alflabs.utils.IClock;
import com.alflabs.utils.ILogger;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

import javax.inject.Named;
import javax.inject.Singleton;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Mockito.mock;

@Module
public abstract class FakeJsonSenderModule {
    @Singleton
    @Provides
    @Named("JsonDateFormat")
    public static DateFormat provideJsonDateFormat() {
        // Format timestamps using ISO 8601
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df;
    }

    @Singleton
    @Provides
    public static JsonSender provideJsonSender(
            ILogger logger,
           FileOps fileOps,
           IClock clock,
           OkHttpClient okHttpClient,
           @Named("JsonDateFormat") DateFormat jsonDateFormat,
           @Named("SingleThreadExecutor") ScheduledExecutorService executor) {
        return new FakeJsonSender(logger, fileOps, clock, okHttpClient, jsonDateFormat, executor);
    }

    @Singleton
    @Provides
    public static FakeJsonSender provideFakeJsonSender(JsonSender jsonSender) {
        return (FakeJsonSender) jsonSender;
    }
}
