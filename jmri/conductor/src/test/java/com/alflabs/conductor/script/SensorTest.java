package com.alflabs.conductor.script;

import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.IJmriSensor;
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
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class SensorTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock IJmriProvider mJmriProvider;
    @Mock IJmriSensor mJmriSensor;
    @Mock IKeyValue mKeyValue;

    private Sensor mSensor;

    @Before
    public void setUp() throws Exception {
        when(mJmriProvider.getSensor("name")).thenReturn(mJmriSensor);

        SensorFactory factory = new SensorFactory(
                InstanceFactory.create(mJmriProvider),
                InstanceFactory.create(mKeyValue));
        mSensor = factory.create("name");
        when(mJmriSensor.isActive()).thenReturn(false);

        mSensor.onExecStart();
        verify(mJmriProvider).getSensor("name");
        verify(mKeyValue).putValue("name", "OFF", true);
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
        verify(mKeyValue).putValue("name", "ON", true);

        reset(mKeyValue);
        when(mJmriSensor.isActive()).thenReturn(false);
        assertThat(mSensor.isActive()).isFalse();
        verify(mKeyValue).putValue("name", "OFF", true);
    }

}
