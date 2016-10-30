package com.alfray.conductor.script;

import com.alfray.conductor.IJmriSensor;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SensorTest {

    private IJmriSensor mJmriSensor;
    private Sensor mSensor;

    @Before
    public void setUp() throws Exception {
        mJmriSensor = mock(IJmriSensor.class);
        mSensor = new Sensor("name", mJmriSensor);
    }

    @Test
    public void testIsActive() throws Exception {
        when(mJmriSensor.isActive()).thenReturn(true);
        assertThat(mSensor.isActive()).isTrue();

        when(mJmriSensor.isActive()).thenReturn(false);
        assertThat(mSensor.isActive()).isFalse();
    }

}
