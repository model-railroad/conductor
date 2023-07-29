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
import com.alflabs.utils.ILogger;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

import java.io.File;

/** Provides a dummy EventLogger that writes to the main ILogger. */
@Module
public abstract class FakeEventLoggerModule {
    @Singleton
    @Provides
    public static EventLogger provideEventLogger(ILogger logger, FileOps fileOps, ILocalDateTimeNowProvider localDateTimeNow) {
        return new EventLogger(logger, fileOps, localDateTimeNow) {
            @Override
            public ILogger getLogger() {
                return super.getLogger();
            }

            @Override
            public void logAsync(Type type, String name, String value) {
                String msg = String.format("<timestamp> - %c - %s - %s",
                        type.name().charAt(0),
                        name,
                        value);

                logger.d("EventLogger", msg);
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
        };
    }
}
