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

package com.alflabs.conductor.v1.script;

import com.alflabs.kv.IKeyValue;
import com.alflabs.rx.IPublisher;
import com.alflabs.rx.IStream;
import com.alflabs.rx.Publishers;
import com.alflabs.rx.Schedulers;
import com.alflabs.rx.Streams;
import dagger.internal.InstanceFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VarTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock IKeyValue mKeyValue;

    private final IStream<String> mChangedStream = Streams.<String>stream().on(Schedulers.sync());

    private Var mStrVar;
    private IStringFunction mStrSet;

    private Var mIntVar;
    private IIntFunction mIntSet;
    private IIntFunction mIntInc;
    private IIntFunction mIntDec;

    @Before
    public void setUp() throws Exception {
        when(mKeyValue.getChangedStream()).thenReturn(mChangedStream);
        
        VarFactory factory = new VarFactory(InstanceFactory.create(mKeyValue));

        mStrVar = factory.create("42", "MyVar");
        mStrSet = mStrVar.createSetStrFunction();

        mIntVar = factory.create(42, "MyVar");
        mIntSet = mIntVar.createSetIntFunction();
        mIntInc = mIntVar.createIncFunction();
        mIntDec = mIntVar.createDecFunction();

        mStrVar.onExecStart();
        mIntVar.onExecStart();
        verify(mKeyValue, never()).putValue(anyString(), anyString(), eq(true));
    }

    @Test
    public void testStrGetSetValue() throws Exception {
        assertThat(mStrVar.get()).isEqualTo("42");

        mStrSet.accept("32");
        assertThat(mStrVar.get()).isEqualTo("32");
    }

    @Test
    public void testIntGetSetValue() throws Exception {
        assertThat(mIntVar.getAsInt()).isEqualTo(42);

        mIntSet.accept(-32);
        assertThat(mIntVar.getAsInt()).isEqualTo(-32);
    }

    @Test
    public void testIntIncValue() throws Exception {
        assertThat(mIntVar.getAsInt()).isEqualTo(42);

        mIntInc.accept(12);
        assertThat(mIntVar.getAsInt()).isEqualTo(54);

        mIntInc.accept(-32);
        assertThat(mIntVar.getAsInt()).isEqualTo(22);
    }

    @Test
    public void testIntDecValue() throws Exception {
        assertThat(mIntVar.getAsInt()).isEqualTo(42);

        mIntDec.accept(12);
        assertThat(mIntVar.getAsInt()).isEqualTo(30);

        mIntDec.accept(-32);
        assertThat(mIntVar.getAsInt()).isEqualTo(62);
    }

    @Test
    public void testIntSetExported() throws Exception {
        verify(mKeyValue, never()).putValue(anyString(), anyString(), eq(true));
        mIntVar.setExported(true);
        mIntSet.accept(43);
        mIntVar.onExecHandle();
        verify(mKeyValue).putValue("V/MyVar", "43", true);
    }

    @Test
    public void testStrSetExported() throws Exception {
        verify(mKeyValue, never()).putValue(anyString(), anyString(), eq(true));
        mStrVar.setExported(true);
        mStrSet.accept("43");
        mStrVar.onExecHandle();
        verify(mKeyValue).putValue("V/MyVar", "43", true);
    }

    @Test
    public void testIntSetImported() throws Exception {
        verify(mKeyValue, never()).putValue(anyString(), anyString(), eq(true));
        mIntVar.setImported(true);

        mIntSet.accept(43);
        assertThat(mIntVar.getAsInt()).isEqualTo(43);

        IPublisher<String> publisher = Publishers.latest();
        mChangedStream.publishWith(publisher);

        when(mKeyValue.getValue("V/MyVar")).thenReturn("44");
        publisher.publish("V/MyVar");
        assertThat(mIntVar.getAsInt()).isEqualTo(44);

        mIntVar.setImported(false);

        when(mKeyValue.getValue("V/MyVar")).thenReturn("45");
        publisher.publish("V/MyVar");
        assertThat(mIntVar.getAsInt()).isEqualTo(44);
    }

    @Test
    public void testStrSetImported() throws Exception {
        verify(mKeyValue, never()).putValue(anyString(), anyString(), eq(true));
        mStrVar.setImported(true);

        mStrSet.accept("two");
        assertThat(mStrVar.get()).isEqualTo("two");

        IPublisher<String> publisher = Publishers.latest();
        mChangedStream.publishWith(publisher);

        when(mKeyValue.getValue("V/MyVar")).thenReturn("three");
        publisher.publish("V/MyVar");
        assertThat(mStrVar.get()).isEqualTo("three");

        mStrVar.setImported(false);

        when(mKeyValue.getValue("V/MyVar")).thenReturn("four");
        publisher.publish("V/MyVar");
        assertThat(mStrVar.get()).isEqualTo("three");
    }

    @Test
    public void testIsActive() throws Exception {
        assertThat(mIntVar.isActive()).isTrue();

        mIntSet.accept(0);
        assertThat(mIntVar.isActive()).isFalse();

        mIntSet.accept(-12);
        assertThat(mIntVar.isActive()).isTrue();
    }
}
