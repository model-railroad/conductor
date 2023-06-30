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

import com.alflabs.conductor.util.Analytics;
import com.alflabs.conductor.util.ILocalDateTimeNowProvider;
import com.alflabs.kv.IKeyValue;
import com.alflabs.utils.FileOps;
import com.alflabs.utils.IClock;
import com.alflabs.utils.ILogger;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;

@Module
public abstract class AnalyticsModule {
    @Singleton
    @Provides
    public static Analytics provideAnalytics(
            ILogger logger,
            IClock clock,
            Random random,
            FileOps fileOps,
            IKeyValue keyValue,
            OkHttpClient okHttpClient,
            ILocalDateTimeNowProvider localDateTimeNow,
            @Named("SingleThreadExecutor") ScheduledExecutorService executor) {
        return new Analytics(
                logger,
                clock,
                random,
                fileOps,
                keyValue,
                okHttpClient,
                localDateTimeNow,
                executor);
    }
}
