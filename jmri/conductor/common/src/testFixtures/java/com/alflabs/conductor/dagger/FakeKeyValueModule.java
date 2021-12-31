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

import com.alflabs.kv.IKeyValue;
import com.alflabs.rx.IStream;
import com.alflabs.rx.Schedulers;
import com.alflabs.rx.Streams;
import com.alflabs.utils.ILogger;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Module
public abstract class FakeKeyValueModule {
    @Singleton
    @Provides
    public static IKeyValue provideKeyValue() {
        return new IKeyValue() {
            private final Map<String, String> mStorage = new HashMap<>();
            private final IStream<String> mChangedStream = Streams.<String>stream().on(Schedulers.sync());

            @Override
            public IStream<String> getChangedStream() {
                return mChangedStream;
            }

            @Override
            public Set<String> getKeys() {
                return mStorage.keySet();
            }

            @Override
            public String getValue(String key) {
                return mStorage.get(key);
            }

            @Override
            public void putValue(String key, String value, boolean broadcast) {
                mStorage.put(key, value);
            }
        };

    }
}
