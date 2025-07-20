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

import com.alflabs.conductor.util.EventLogger;
import com.alflabs.conductor.util.ILocalDateTimeNowProvider;
import com.alflabs.conductor.util.JsonSender;
import com.alflabs.utils.FileOps;
import com.alflabs.utils.IClock;
import com.alflabs.utils.ILogger;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/** Provides a dummy EventLogger that writes to the main ILogger. */
public class FakeJsonSender extends JsonSender {

    private final ArrayList<String> mEvents = new ArrayList<>();
    private final IClock mClock;

    @Inject
    public FakeJsonSender(
            ILogger logger,
            FileOps fileOps,
            IClock clock,
            OkHttpClient okHttpClient,
            @Named("JsonDateFormat") DateFormat jsonDateFormat,
            @Named("SingleThreadExecutor") ScheduledExecutorService executor) {
        super(logger, fileOps, clock, okHttpClient, jsonDateFormat, executor);
        mClock = clock;
    }

    @Override
    public void shutdown() throws InterruptedException {
        // no-op
    }

    @Override
    public void setJsonUrl(String urlOrFile) throws IOException {
        // no-op
    }

    @Override
    public HttpUrl getJsonUrl() {
        return HttpUrl.parse("http://alfray.com/rtac/testing");
    }

    @Override
    public void sendEvent(String key1, String key2, String value) {
        // Do not log to the main logger (this results in duplicates in testing).
        String msg = String.format("<clock %d> - %s/%s = %s",
                mClock.elapsedRealtime(),
                key1,
                key2,
                value);
        synchronized (mEvents) {
            mEvents.add(msg);
        }
    }

    public List<String> eventsGetAndClear() {
        ArrayList<String> copy;
        synchronized (mEvents) {
            copy = new ArrayList<>(mEvents);
            mEvents.clear();
        }
        return copy;
    }
}

