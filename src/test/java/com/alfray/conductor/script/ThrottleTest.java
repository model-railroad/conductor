package com.alfray.conductor.script;

import com.alfray.conductor.IJmriProvider;
import com.alfray.conductor.IJmriThrottle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ThrottleTest {
    private IJmriThrottle mJmriThrottle;
    private Throttle mThrottle;
    private IFunction.Int fwd;
    private IFunction.Int rev;
    private IFunction.Int stop;
    private IFunction.Int sound;
    private IFunction.Int light;
    private IConditional isFwd;
    private IConditional isRev;
    private IConditional isStop;
    private IConditional isSound;
    private IConditional isLight;

    @Before
    public void setUp() throws Exception {
        mJmriThrottle = mock(IJmriThrottle.class);

        IJmriProvider provider = mock(IJmriProvider.class);
        when(provider.getThrotlle(42)).thenReturn(mJmriThrottle);

        mThrottle = new Throttle(42);

        mThrottle.init(provider);
        verify(provider).getThrotlle(42);

        fwd = mThrottle.createFunctionForward();
        rev = mThrottle.createFunctionReverse();
        stop = mThrottle.createFunctionStop();
        sound = mThrottle.createFunctionSound();
        light = mThrottle.createFunctionLight();
        isFwd = mThrottle.createIsForward();
        isRev = mThrottle.createIsReverse();
        isStop = mThrottle.createIsStopped();
        isSound = mThrottle.createIsSound();
        isLight = mThrottle.createIsLight();
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mJmriThrottle);
    }

    @Test
    public void testInit() throws Exception {
        assertThat(isFwd.isActive()).isFalse();
        assertThat(isRev.isActive()).isFalse();
        assertThat(isStop.isActive()).isTrue();
    }

    @Test
    public void testForward() throws Exception {
        fwd.setValue(42);
        verify(mJmriThrottle).setSpeed(42);

        assertThat(isFwd.isActive()).isTrue();
        assertThat(isRev.isActive()).isFalse();
        assertThat(isStop.isActive()).isFalse();

        fwd.setValue(-42);
        verify(mJmriThrottle).setSpeed(0);

        assertThat(isFwd.isActive()).isFalse();
        assertThat(isRev.isActive()).isFalse();
        assertThat(isStop.isActive()).isTrue();
    }

    @Test
    public void testReverse() throws Exception {
        rev.setValue(42);
        verify(mJmriThrottle).setSpeed(-42);

        assertThat(isFwd.isActive()).isFalse();
        assertThat(isRev.isActive()).isTrue();
        assertThat(isStop.isActive()).isFalse();

        rev.setValue(-42);
        verify(mJmriThrottle).setSpeed(0);

        assertThat(isFwd.isActive()).isFalse();
        assertThat(isRev.isActive()).isFalse();
        assertThat(isStop.isActive()).isTrue();
    }

    @Test
    public void testStop() throws Exception {
        fwd.setValue(42);
        verify(mJmriThrottle).setSpeed(42);

        stop.setValue(0); // value is irrelevant
        verify(mJmriThrottle).setSpeed(0);
        assertThat(isFwd.isActive()).isFalse();
        assertThat(isRev.isActive()).isFalse();
        assertThat(isStop.isActive()).isTrue();
    }

    @Test
    public void testSound() throws Exception {
        assertThat(isSound.isActive()).isFalse();

        sound.setValue(0);
        assertThat(isSound.isActive()).isFalse();
        verify(mJmriThrottle).setSound(false);

        sound.setValue(1);
        assertThat(isSound.isActive()).isTrue();
        verify(mJmriThrottle).setSound(true);
        reset(mJmriThrottle);

        sound.setValue(0);
        assertThat(isSound.isActive()).isFalse();
        verify(mJmriThrottle).setSound(false);
    }

    @Test
    public void testLight() throws Exception {
        assertThat(isLight.isActive()).isFalse();

        light.setValue(0);
        assertThat(isLight.isActive()).isFalse();
        verify(mJmriThrottle).setLight(false);

        light.setValue(1);
        assertThat(isLight.isActive()).isTrue();
        verify(mJmriThrottle).setLight(true);
        reset(mJmriThrottle);

        light.setValue(0);
        assertThat(isLight.isActive()).isFalse();
        verify(mJmriThrottle).setLight(false);
    }
}
