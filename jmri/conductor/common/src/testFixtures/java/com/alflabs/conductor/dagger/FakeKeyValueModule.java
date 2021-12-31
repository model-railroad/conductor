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
    public static IKeyValue provideKeyValue(ILogger logger) {
//        IKeyValue keyValue = mock(IKeyValue.class);
//
//        // Implement KeyValue getValue() and putValue() using a simple object map backend.
//        Map<Object, Object> storage = new HashMap<>();
//        doAnswer(new Answer() {
//            @Override
//            public Object answer(InvocationOnMock invocation) throws Throwable {
//                storage.put(invocation.getArgument(0), invocation.getArgument(1));
//                return null;
//            }
//        }).when(keyValue).putValue(any(), any(), anyBoolean());
//
//        when(keyValue.getValue(any())).thenAnswer(new Answer<Object>() {
//            @Override
//            public Object answer(InvocationOnMock invocation) throws Throwable {
//                return storage.get(invocation.getArgument(0));
//            }
//        });
//
//        return keyValue;

        return new IKeyValue() {
            Map<String, String> mStorage = new HashMap<>();

            @Override
            public IStream<String> getChangedStream() {
                return null;
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
