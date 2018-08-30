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

package com.alflabs.conductor.script;

import com.alflabs.kv.IKeyValue;
import com.alflabs.rx.IPublisher;
import com.alflabs.rx.IStream;
import com.alflabs.rx.Publishers;
import com.alflabs.rx.Schedulers;
import com.alflabs.rx.Streams;
import dagger.internal.InstanceFactory;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EnumTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock IKeyValue mKeyValue;

    private final IStream<String> mChangedStream = Streams.<String>stream().on(Schedulers.sync());

    private Enum_ mEnum;

    @Before
    public void setUp() throws Exception {
        when(mKeyValue.getChangedStream()).thenReturn(mChangedStream);

        EnumFactory factory = new EnumFactory(InstanceFactory.create(mKeyValue));
        mEnum = factory.create(Arrays.asList("one", "two", "three"), "MyVar");
        assertThat(mEnum.get()).isEqualTo("one");

        mEnum.onExecStart();
        verify(mKeyValue, never()).putValue(anyString(), anyString(), eq(true));
    }

    @Test
    public void testGetSetValue() throws Exception {
        mEnum.accept("Three");
        assertThat(mEnum.get()).isEqualTo("three");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetValue_Invalid() throws Exception {
        mEnum.accept("Invalid");
        Assert.fail("Expected IllegalArgumentException");
    }

    @Test
    public void testSetExported() throws Exception {
        verify(mKeyValue, never()).putValue(anyString(), anyString(), eq(true));
        mEnum.setExported(true);
        mEnum.accept("two");
        mEnum.onExecHandle();
        verify(mKeyValue).putValue("V/MyVar", "two", true);
    }

    @Test
    public void testSetImported() throws Exception {
        verify(mKeyValue, never()).putValue(anyString(), anyString(), eq(true));
        mEnum.setImported(true);

        mEnum.accept("two");
        assertThat(mEnum.get()).isEqualTo("two");

        IPublisher<String> publisher = Publishers.latest();
        mChangedStream.publishWith(publisher);

        when(mKeyValue.getValue("V/MyVar")).thenReturn("three");
        publisher.publish("V/MyVar");
        assertThat(mEnum.get()).isEqualTo("three");

        mEnum.setImported(false);

        when(mKeyValue.getValue("V/MyVar")).thenReturn("four");
        publisher.publish("V/MyVar");
        assertThat(mEnum.get()).isEqualTo("three");
    }

    @Test
    public void testFunctionEQ() throws Exception {
        IConditional eq = mEnum.createCondition("==", "One");
        assertThat(eq.isActive()).isTrue();

        mEnum.accept("Two");
        assertThat(eq.isActive()).isFalse();
    }

    @Test
    public void testFunctionNEQ() throws Exception {
        IConditional neq = mEnum.createCondition("!=", "One");
        assertThat(neq.isActive()).isFalse();

        mEnum.accept("Two");
        assertThat(neq.isActive()).isTrue();
    }
}
