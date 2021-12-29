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

import com.alflabs.conductor.jmri.IJmriProvider;
import com.alflabs.conductor.jmri.IJmriTurnout;
import com.alflabs.kv.IKeyValue;
import com.google.common.truth.Truth;
import dagger.internal.InstanceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class TurnoutTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock IJmriProvider mJmriProvider;
    @Mock IJmriTurnout mJmriTurnout;
    @Mock IKeyValue mKeyValue;

    private Turnout mTurnout;

    @Before
    public void setUp() throws Exception {
        mJmriTurnout = mock(IJmriTurnout.class);
        when(mJmriTurnout.isNormal()).thenReturn(IJmriTurnout.NORMAL);

        when(mJmriProvider.getTurnout("jmriName")).thenReturn(mJmriTurnout);

        TurnoutFactory factory = new TurnoutFactory(
                InstanceFactory.create(mJmriProvider),
                InstanceFactory.create(mKeyValue));
        mTurnout = factory.create("jmriName", "scriptName");

        mTurnout.onExecStart();
        verify(mJmriTurnout).isNormal();
        reset(mJmriTurnout);
        verify(mJmriProvider).getTurnout("jmriName");
        verify(mKeyValue).putValue("T/scriptName", "N", true);
        reset(mKeyValue);

        Truth.assertThat(mTurnout.isActive()).isTrue();
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mJmriTurnout);
        verifyNoMoreInteractions(mKeyValue);
    }

    @Test
    public void testNormal() throws Exception {
        mTurnout.createFunction(Turnout.Function.NORMAL).accept(0);
        verify(mJmriTurnout).setTurnout(IJmriTurnout.NORMAL);
        verify(mJmriTurnout, never()).setTurnout(IJmriTurnout.REVERSE);
        when(mJmriTurnout.isNormal()).thenReturn(IJmriTurnout.NORMAL);

        verify(mKeyValue, never()).putValue(anyString(), anyString(), anyBoolean());
        mTurnout.onExecHandle();
        verify(mJmriTurnout).isNormal();
        verify(mKeyValue).putValue("T/scriptName", "N", true);

        Truth.assertThat(mTurnout.isActive()).isTrue();
    }

    @Test
    public void testReverse() throws Exception {
        mTurnout.createFunction(Turnout.Function.REVERSE).accept(0);
        verify(mJmriTurnout, never()).setTurnout(IJmriTurnout.NORMAL);
        verify(mJmriTurnout).setTurnout(IJmriTurnout.REVERSE);
        when(mJmriTurnout.isNormal()).thenReturn(IJmriTurnout.REVERSE);

        verify(mKeyValue, never()).putValue(anyString(), anyString(), anyBoolean());
        mTurnout.onExecHandle();
        verify(mJmriTurnout).isNormal();
        verify(mKeyValue).putValue("T/scriptName", "R", true);

        Truth.assertThat(mTurnout.isActive()).isFalse();
    }
}
