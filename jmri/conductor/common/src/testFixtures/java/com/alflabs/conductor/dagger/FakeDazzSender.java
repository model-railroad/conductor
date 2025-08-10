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

import com.alflabs.dazzserv.store.DataEntry;
import com.alflabs.conductor.util.DazzSender;
import com.alflabs.utils.FileOps;
import com.alflabs.utils.ILogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/** Provides a dummy DazzSender that writes to the main ILogger. */
public class FakeDazzSender extends DazzSender {

    private final ArrayList<String> mEvents = new ArrayList<>();

    @Inject
    public FakeDazzSender(
            ILogger logger,
            FileOps fileOps,
            OkHttpClient okHttpClient,
            @Named("JsonDateFormat") DateFormat jsonDateFormat,
            @Named("SingleThreadExecutor") ScheduledExecutorService executor) {
        super(logger, fileOps, okHttpClient, jsonDateFormat, executor);
    }

    @Override
    public void shutdown() throws InterruptedException {
        // no-op
    }

    @Override
    public void setDazzUrl(String urlOrFile) throws IOException {
        // no-op
    }

    @Override
    public HttpUrl getDazzUrl() {
        return HttpUrl.parse("http://alfray.com/rtac/testing/store");
    }

    @Override
    public void sendEvent(String key, long eventTimestampMs, boolean state, String payload) {
        // We actually do not override sendEvent, to let the original
        // class perform all its (overcomplicated) behavior.
        // Instead, this will set mLatestJson and then call scheduleSend()
        // which we intercept below.
        super.sendEvent(key, eventTimestampMs, state, payload);
    }

    @Override
    protected void scheduleSend() {
        DataEntry entry = mEventQueue.pollFirst();
        if (entry != null) {
            try {
                String jsonData = entry.toJsonString(/*mapper=*/ null);
                synchronized (mEvents) {
                    mEvents.add(jsonData);
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException("FakeDazzSender JSON conversion failed", e);
            }
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

