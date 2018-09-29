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

import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.IJmriSensor;
import com.alflabs.conductor.util.EventLogger;
import com.alflabs.kv.IKeyValue;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class SensorTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock IJmriSensor mJmriSensor;
    @Mock IJmriProvider mJmriProvider;
    @Mock IKeyValue mKeyValue;
    @Mock EventLogger mEventLogger;
    @Mock Runnable mOnChangeRunnable;

    private Sensor mSensor;

    @Before
    public void setUp() throws Exception {
        when(mJmriProvider.getSensor("jmriName")).thenReturn(mJmriSensor);
        when(mJmriSensor.isActive()).thenReturn(false);

        SensorFactory factory = new SensorFactory(
                InstanceFactory.create(mJmriProvider),
                InstanceFactory.create(mKeyValue),
                InstanceFactory.create(mEventLogger));
        mSensor = factory.create("jmriName", "scriptName");
        mSensor.setOnChangedListener(mOnChangeRunnable);

        assertThat(mSensor.getJmriSensor()).isNull();

        mSensor.onExecStart();
        verify(mJmriProvider).getSensor("jmriName");
        verify(mKeyValue).putValue("S/scriptName", "OFF", true);
        assertThat(mSensor.getJmriSensor()).isSameAs(mJmriSensor);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mJmriProvider);
        verifyNoMoreInteractions(mKeyValue);
    }

    @Test
    public void testIsActive() throws Exception {
        reset(mKeyValue);
        when(mJmriSensor.isActive()).thenReturn(true);
        assertThat(mSensor.isActive()).isTrue();
        verify(mOnChangeRunnable, never()).run();

        verify(mKeyValue, never()).putValue(anyString(), anyString(), anyBoolean());
        mSensor.onExecHandle();
        verify(mKeyValue).putValue("S/scriptName", "ON", true);
        verify(mOnChangeRunnable).run();

        reset(mKeyValue);
        reset(mOnChangeRunnable);

        when(mJmriSensor.isActive()).thenReturn(false);
        assertThat(mSensor.isActive()).isFalse();

        verify(mKeyValue, never()).putValue(anyString(), anyString(), anyBoolean());
        mSensor.onExecHandle();
        verify(mKeyValue).putValue("S/scriptName", "OFF", true);
        verify(mOnChangeRunnable).run();
    }

}
