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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class SensorTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock IJmriProvider mJmriProvider;
    @Mock IJmriSensor mJmriSensor;
    @Mock IKeyValue mKeyValue;
    @Mock Runnable mOnChangeRunnable;

    private Sensor mSensor;

    @Before
    public void setUp() throws Exception {
        when(mJmriProvider.getSensor("jmriName")).thenReturn(mJmriSensor);
        when(mJmriSensor.isActive()).thenReturn(false);

        SensorFactory factory = new SensorFactory(
                InstanceFactory.create(mJmriProvider),
                InstanceFactory.create(mKeyValue));
        mSensor = factory.create("jmriName", "scriptName");
        mSensor.setOnChangedListener(mOnChangeRunnable);

        assertThat(mSensor.getJmriSensor()).isNull();

        mSensor.onExecStart();
        verify(mJmriProvider).getSensor("jmriName");
        verify(mKeyValue).putValue("scriptName", "OFF", true);
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
        verify(mKeyValue).putValue("scriptName", "ON", true);
        verify(mOnChangeRunnable).run();

        reset(mKeyValue);
        reset(mOnChangeRunnable);

        when(mJmriSensor.isActive()).thenReturn(false);
        assertThat(mSensor.isActive()).isFalse();

        verify(mKeyValue, never()).putValue(anyString(), anyString(), anyBoolean());
        mSensor.onExecHandle();
        verify(mKeyValue).putValue("scriptName", "OFF", true);
        verify(mOnChangeRunnable).run();
    }

}
