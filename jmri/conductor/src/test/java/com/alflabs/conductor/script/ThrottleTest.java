package com.alflabs.conductor.script;

import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.IJmriThrottle;
import com.alflabs.kv.IKeyValue;
import dagger.internal.InstanceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Collections;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ThrottleTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock IJmriProvider mJmriProvider;
    @Mock IJmriThrottle mJmriThrottle;
    @Mock IKeyValue mKeyValue;

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
        when(mJmriProvider.getThrotlle(42)).thenReturn(mJmriThrottle);

        ThrottleFactory factory = new ThrottleFactory(
                InstanceFactory.create(mJmriProvider),
                InstanceFactory.create(mKeyValue));
        mThrottle = factory.create(Collections.singletonList(42));
        assertThat(mThrottle.getDccAddressesAsString()).isEqualTo("42");
        when(mJmriThrottle.getDccAddress()).thenReturn(42);

        mThrottle.onExecStart();
        verify(mJmriProvider).getThrotlle(42);
        verify(mJmriThrottle, atLeastOnce()).getDccAddress();
        verify(mKeyValue).putValue("D/42", "0", true);
        reset(mKeyValue);

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
        verifyNoMoreInteractions(mKeyValue);
    }

    @Test
    public void testInit() throws Exception {
        assertThat(isFwd.isActive()).isFalse();
        assertThat(isRev.isActive()).isFalse();
        assertThat(isStop.isActive()).isTrue();
    }

    @Test
    public void testForward() throws Exception {
        fwd.accept(41);
        verify(mJmriThrottle).setSpeed(41);
        verify(mKeyValue).putValue("D/42", "41", true);

        assertThat(isFwd.isActive()).isTrue();
        assertThat(isRev.isActive()).isFalse();
        assertThat(isStop.isActive()).isFalse();

        fwd.accept(-43);
        verify(mJmriThrottle).setSpeed(0);
        verify(mKeyValue).putValue("D/42", "0", true);

        assertThat(isFwd.isActive()).isFalse();
        assertThat(isRev.isActive()).isFalse();
        assertThat(isStop.isActive()).isTrue();

        verify(mJmriThrottle, atLeastOnce()).getDccAddress();
    }

    @Test
    public void testReverse() throws Exception {
        rev.accept(41);
        verify(mJmriThrottle).setSpeed(-41);
        verify(mKeyValue).putValue("D/42", "-41", true);

        assertThat(isFwd.isActive()).isFalse();
        assertThat(isRev.isActive()).isTrue();
        assertThat(isStop.isActive()).isFalse();

        rev.accept(-43);
        verify(mJmriThrottle).setSpeed(0);
        verify(mKeyValue).putValue("D/42", "0", true);

        assertThat(isFwd.isActive()).isFalse();
        assertThat(isRev.isActive()).isFalse();
        assertThat(isStop.isActive()).isTrue();

        verify(mJmriThrottle, atLeastOnce()).getDccAddress();
    }

    @Test
    public void testStop() throws Exception {
        fwd.accept(41);
        verify(mJmriThrottle).setSpeed(41);
        verify(mKeyValue).putValue("D/42", "41", true);

        stop.accept(0); // value is irrelevant
        verify(mJmriThrottle).setSpeed(0);
        verify(mKeyValue).putValue("D/42", "0", true);
        assertThat(isFwd.isActive()).isFalse();
        assertThat(isRev.isActive()).isFalse();
        assertThat(isStop.isActive()).isTrue();

        verify(mJmriThrottle, atLeastOnce()).getDccAddress();
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
