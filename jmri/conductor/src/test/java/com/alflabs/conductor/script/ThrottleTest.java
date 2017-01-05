package com.alflabs.conductor.script;

import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.IJmriThrottle;
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
    private IIntFunction fwd;
    private IIntFunction rev;
    private IIntFunction stop;
    private IIntFunction sound;
    private IIntFunction light;
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
        assertThat(mThrottle.getDccAddresses()).isEqualTo("42");

        mThrottle.setup(provider);
        verify(provider).getThrotlle(42);

        fwd = mThrottle.createFunction(Throttle.Function.FORWARD);
        rev = mThrottle.createFunction(Throttle.Function.REVERSE);
        stop = mThrottle.createFunction(Throttle.Function.STOP);
        sound = mThrottle.createFunction(Throttle.Function.SOUND);
        light = mThrottle.createFunction(Throttle.Function.LIGHT);
        isFwd = mThrottle.createCondition(Throttle.Condition.FORWARD);
        isRev = mThrottle.createCondition(Throttle.Condition.REVERSE);
        isStop = mThrottle.createCondition(Throttle.Condition.STOPPED);
        isSound = mThrottle.createCondition(Throttle.Condition.SOUND);
        isLight = mThrottle.createCondition(Throttle.Condition.LIGHT);
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
        fwd.accept(42);
        verify(mJmriThrottle).setSpeed(42);

        assertThat(isFwd.isActive()).isTrue();
        assertThat(isRev.isActive()).isFalse();
        assertThat(isStop.isActive()).isFalse();

        fwd.accept(-42);
        verify(mJmriThrottle).setSpeed(0);

        assertThat(isFwd.isActive()).isFalse();
        assertThat(isRev.isActive()).isFalse();
        assertThat(isStop.isActive()).isTrue();
    }

    @Test
    public void testReverse() throws Exception {
        rev.accept(42);
        verify(mJmriThrottle).setSpeed(-42);

        assertThat(isFwd.isActive()).isFalse();
        assertThat(isRev.isActive()).isTrue();
        assertThat(isStop.isActive()).isFalse();

        rev.accept(-42);
        verify(mJmriThrottle).setSpeed(0);

        assertThat(isFwd.isActive()).isFalse();
        assertThat(isRev.isActive()).isFalse();
        assertThat(isStop.isActive()).isTrue();
    }

    @Test
    public void testStop() throws Exception {
        fwd.accept(42);
        verify(mJmriThrottle).setSpeed(42);

        stop.accept(0); // value is irrelevant
        verify(mJmriThrottle).setSpeed(0);
        assertThat(isFwd.isActive()).isFalse();
        assertThat(isRev.isActive()).isFalse();
        assertThat(isStop.isActive()).isTrue();
    }

    @Test
    public void testSound() throws Exception {
        assertThat(isSound.isActive()).isFalse();

        sound.accept(0);
        assertThat(isSound.isActive()).isFalse();
        verify(mJmriThrottle).setSound(false);

        sound.accept(1);
        assertThat(isSound.isActive()).isTrue();
        verify(mJmriThrottle).setSound(true);
        reset(mJmriThrottle);

        sound.accept(0);
        assertThat(isSound.isActive()).isFalse();
        verify(mJmriThrottle).setSound(false);
    }

    @Test
    public void testLight() throws Exception {
        assertThat(isLight.isActive()).isFalse();

        light.accept(0);
        assertThat(isLight.isActive()).isFalse();
        verify(mJmriThrottle).setLight(false);

        light.accept(1);
        assertThat(isLight.isActive()).isTrue();
        verify(mJmriThrottle).setLight(true);
        reset(mJmriThrottle);

        light.accept(0);
        assertThat(isLight.isActive()).isFalse();
        verify(mJmriThrottle).setLight(false);
    }

    @Test
    public void testFnFunction() throws Exception {
        mThrottle.createFnFunction(3).accept(1);
        verify(mJmriThrottle).triggerFunction(3, true);

        mThrottle.createFnFunction(5).accept(0);
        verify(mJmriThrottle).triggerFunction(5, false);

        mThrottle.createFnFunction(12).accept(1);
        verify(mJmriThrottle).triggerFunction(12, true);

        mThrottle.createFnFunction(28).accept(0);
        verify(mJmriThrottle).triggerFunction(28, false);
    }
}
