package com.alflabs.conductor.script;

import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.IJmriTurnout;
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

        when(mJmriProvider.getTurnout("jmriName")).thenReturn(mJmriTurnout);

        TurnoutFactory factory = new TurnoutFactory(
                InstanceFactory.create(mJmriProvider),
                InstanceFactory.create(mKeyValue));
        mTurnout = factory.create("jmriName", "scriptName");

        mTurnout.onExecStart();
        verify(mJmriProvider).getTurnout("jmriName");
        verify(mKeyValue).putValue("scriptName", "N", true);
        reset(mKeyValue);

        assertThat(mTurnout.isActive()).isTrue();
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

        verify(mKeyValue, never()).putValue(anyString(), anyString(), anyBoolean());
        mTurnout.onExecHandle();
        verify(mKeyValue).putValue("scriptName", "N", true);

        assertThat(mTurnout.isActive()).isTrue();
    }

    @Test
    public void testReverse() throws Exception {
        mTurnout.createFunction(Turnout.Function.REVERSE).accept(0);
        verify(mJmriTurnout, never()).setTurnout(IJmriTurnout.NORMAL);
        verify(mJmriTurnout).setTurnout(IJmriTurnout.REVERSE);

        verify(mKeyValue, never()).putValue(anyString(), anyString(), anyBoolean());
        mTurnout.onExecHandle();
        verify(mKeyValue).putValue("scriptName", "R", true);

        assertThat(mTurnout.isActive()).isFalse();
    }
}
