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

public class VarTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock IKeyValue mKeyValue;

    private Var mVar;
    private IIntFunction mFuncSet;
    private IIntFunction mFuncInc;
    private IIntFunction mFuncDec;

    @Before
    public void setUp() throws Exception {
        VarFactory factory = new VarFactory(InstanceFactory.create(mKeyValue));
        mVar = factory.create(42, "MyVar");

        mFuncSet = mVar.createSetterFunction();
        mFuncInc = mVar.createIncFunction();
        mFuncDec = mVar.createDecFunction();

        mVar.onExecStart();
        verify(mKeyValue, never()).putValue(anyString(), anyString(), eq(true));
    }

    @Test
    public void testGetSetValue() throws Exception {
        assertThat(mVar.getAsInt()).isEqualTo(42);

        mFuncSet.accept(-32);
        assertThat(mVar.getAsInt()).isEqualTo(-32);
    }

    @Test
    public void testIncValue() throws Exception {
        assertThat(mVar.getAsInt()).isEqualTo(42);

        mFuncInc.accept(12);
        assertThat(mVar.getAsInt()).isEqualTo(54);

        mFuncInc.accept(-32);
        assertThat(mVar.getAsInt()).isEqualTo(22);
    }

    @Test
    public void testDecValue() throws Exception {
        assertThat(mVar.getAsInt()).isEqualTo(42);

        mFuncDec.accept(12);
        assertThat(mVar.getAsInt()).isEqualTo(30);

        mFuncDec.accept(-32);
        assertThat(mVar.getAsInt()).isEqualTo(62);
    }

    @Test
    public void testSetExported() throws Exception {
        verify(mKeyValue, never()).putValue(anyString(), anyString(), eq(true));
        mVar.setExported(true);
        mFuncSet.accept(43);
        mVar.onExecHandle();
        verify(mKeyValue).putValue("V/MyVar", "43", true);
    }

    @Test
    public void testIsActive() throws Exception {
        assertThat(mVar.isActive()).isTrue();

        mFuncSet.accept(0);
        assertThat(mVar.isActive()).isFalse();

        mFuncSet.accept(-12);
        assertThat(mVar.isActive()).isTrue();
    }
}
