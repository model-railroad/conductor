/*
 * Project: Conductor
 * Copyright (C) 2017 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alflabs.conductor.script;

import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.IJmriThrottle;
import com.alflabs.conductor.util.Logger;
import com.alflabs.kv.IKeyValue;
import com.alflabs.utils.MockClock;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ThrottleTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock IJmriProvider mJmriProvider;
    @Mock IJmriThrottle mJmriThrottle;
    @Mock IKeyValue mKeyValue;
    @Mock Logger mLogger;

    private Throttle mThrottle;
    private MockClock mClock;

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
        mClock = new MockClock();
        when(mJmriProvider.getThrotlle(42)).thenReturn(mJmriThrottle);

        ThrottleFactory factory = new ThrottleFactory(
                InstanceFactory.create(mClock),
                InstanceFactory.create(mLogger),
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

    @Test
    public void testRepeatSpeed() throws Exception {
        fwd.accept(41);
        mThrottle.repeatSpeed();
        verify(mJmriThrottle, times(1)).setSpeed(41);

        mClock.sleep(500);
        mThrottle.repeatSpeed();
        verify(mJmriThrottle, times(1)).setSpeed(41);

        mClock.sleep(500);
        mThrottle.repeatSpeed();
        verify(mJmriThrottle, times(2)).setSpeed(41);
        verify(mKeyValue).putValue("D/42", "41", true);
    }
}
