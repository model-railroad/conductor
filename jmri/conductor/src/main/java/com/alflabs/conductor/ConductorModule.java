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

import com.alflabs.utils.FileOps;
import com.alflabs.conductor.util.Logger;
import com.alflabs.conductor.util.Now;
import com.alflabs.kv.KeyValueServer;
import com.alflabs.utils.ILogger;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class ConductorModule {
    private final IJmriProvider mJmriProvider;

    public ConductorModule(IJmriProvider jmriProvider) {
        mJmriProvider = jmriProvider;
    }

    @Singleton
    @Provides
    public Now provideNowProvider() {
        return new Now();
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
