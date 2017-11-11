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

import com.alflabs.conductor.util.FakeNow;
import dagger.internal.InstanceFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static com.google.common.truth.Truth.assertThat;

public class TimerTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    private FakeNow mNow;
    private Timer mTimer;

    @Before
    public void setUp() throws Exception {
        mNow = new FakeNow(100*1000);

        TimerFactory factory = new TimerFactory(InstanceFactory.create(mNow));
        mTimer = factory.create(42);
    }

    @Test
    public void testTimer() throws Exception {

        assertThat(mTimer.isActive()).isFalse();

        mTimer.createFunction(Timer.Function.START).accept(1);
        assertThat(mTimer.isActive()).isFalse();

        mNow.setNow(141*1000);
        assertThat(mTimer.isActive()).isFalse();

        mNow.setNow(142*1000);
        assertThat(mTimer.isActive()).isTrue();

        // timer stays active till reset using end
        mNow.setNow(200*1000);
        assertThat(mTimer.isActive()).isTrue();

        mTimer.createFunction(Timer.Function.END).accept(1);
        assertThat(mTimer.isActive()).isFalse();
    }

    @Test
    public void testTimerReset() throws Exception {
        FakeNow now = new FakeNow(100*1000);
        Timer timer = new Timer(42, now);

        assertThat(timer.isActive()).isFalse();

        timer.createFunction(Timer.Function.START).accept(1);
        assertThat(timer.isActive()).isFalse();

        // timer active now
        now.setNow(200*1000);
        assertThat(timer.isActive()).isTrue();

        timer.reset();
        assertThat(timer.isActive()).isFalse();
    }
}
