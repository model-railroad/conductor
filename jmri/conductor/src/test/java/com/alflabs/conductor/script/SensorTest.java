package com.alflabs.conductor.script;

import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.IJmriSensor;
import dagger.internal.InstanceFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SensorTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock IJmriProvider mJmriProvider;
    @Mock IJmriSensor mJmriSensor;

    private Sensor mSensor;

    @Before
    public void setUp() throws Exception {
        when(mJmriProvider.getSensor("name")).thenReturn(mJmriSensor);

        SensorFactory factory = new SensorFactory(InstanceFactory.create(mJmriProvider));
        mSensor = factory.create("name");

        mSensor.onExecStart();
        verify(mJmriProvider).getSensor("name");
    }

    @Test
    public void testIsActive() throws Exception {
        when(mJmriSensor.isActive()).thenReturn(true);
        assertThat(mSensor.isActive()).isTrue();

        when(mJmriSensor.isActive()).thenReturn(false);
        assertThat(mSensor.isActive()).isFalse();
    }

}
