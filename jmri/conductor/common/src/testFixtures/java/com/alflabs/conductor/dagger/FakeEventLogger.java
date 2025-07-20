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
import com.alflabs.utils.FileOps;
import com.alflabs.utils.IClock;
import com.alflabs.utils.ILogger;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Provides a dummy EventLogger that writes to the main ILogger. */
public class FakeEventLogger extends EventLogger {

    private final IClock mClock;
    private final ArrayList<String> mLogs = new ArrayList<>();

    @Inject
    public FakeEventLogger(
            IClock clock,
            ILogger logger,
            FileOps fileOps,
            ILocalDateTimeNowProvider localDateTimeNow) {
        super(logger, fileOps, localDateTimeNow);
        mClock = clock;
    }

    @Override
    public ILogger getLogger() {
        return super.getLogger();
    }

    @Override
    public void logAsync(Type type, String name, String value) {
        String msg = String.format("<clock %d> - %c - %s - %s",
                mClock.elapsedRealtime(),
                type.name().charAt(0),
                name,
                value);

        getLogger().d("EventLogger", msg);
        synchronized (mLogs) {
            System.out.println("@@ LOG ADD to " + this);
            mLogs.add(msg);
        }
    }

    @Override
    public String start(File logDirectory) {
        // Path does not denote a real file since this doesn't write.
        return "/tmp/_fake_event_log.txt";
    }

    @Override
    public void shutdown() throws InterruptedException {
        // no-op
    }

    public List<String> eventLogGetAndClear() {
        System.out.println("@@ LOG READ from " + this);
        ArrayList<String> copy = new ArrayList<>(mLogs);
        mLogs.clear();
        return copy;
    }
}

