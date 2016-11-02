package com.alfray.conductor.script;

import com.alfray.conductor.IJmriProvider;
import com.alfray.conductor.IJmriSensor;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SensorTest {

    private IJmriSensor mJmriSensor;
    private Sensor mSensor;

    @Before
    public void setUp() throws Exception {
        mJmriSensor = mock(IJmriSensor.class);

        IJmriProvider provider = mock(IJmriProvider.class);
        when(provider.getSensor("name")).thenReturn(mJmriSensor);

        mSensor = new Sensor("name");

        mSensor.setup(provider);
        verify(provider).getSensor("name");
    }

    @Test
    public void testIsActive() throws Exception {
        when(mJmriSensor.isActive()).thenReturn(true);
        assertThat(mSensor.isActive()).isTrue();

        when(mJmriSensor.isActive()).thenReturn(false);
        assertThat(mSensor.isActive()).isFalse();
    }

}
