package com.alfray.conductor.script;

import com.alfray.conductor.IJmriProvider;
import com.alfray.conductor.IJmriTurnout;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class TurnoutTest {
    private IJmriTurnout mJmriTurnout;
    private Turnout mTurnout;

    @Before
    public void setUp() throws Exception {
        mJmriTurnout = mock(IJmriTurnout.class);

        IJmriProvider provider = mock(IJmriProvider.class);
        when(provider.getTurnout("name")).thenReturn(mJmriTurnout);

        mTurnout = new Turnout("name");

        mTurnout.setup(provider);
        verify(provider).getTurnout("name");
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mJmriTurnout);
    }

    @Test
    public void testNormal() throws Exception {
        mTurnout.createFunctionNormal().setValue(0);
        verify(mJmriTurnout).setTurnout(IJmriTurnout.NORMAL);
        verify(mJmriTurnout, never()).setTurnout(IJmriTurnout.REVERSE);
    }

    @Test
    public void testReverse() throws Exception {
        mTurnout.createFunctionReverse().setValue(0);
        verify(mJmriTurnout, never()).setTurnout(IJmriTurnout.NORMAL);
        verify(mJmriTurnout).setTurnout(IJmriTurnout.REVERSE);
    }
}
