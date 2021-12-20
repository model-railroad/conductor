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

package com.alflabs.conductor.v1.script;

import com.alflabs.conductor.util.EventLogger;
import com.alflabs.utils.FakeClock;
import com.alflabs.utils.ILogger;
import dagger.internal.InstanceFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static com.google.common.truth.Truth.assertThat;

public class TimerTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock private ILogger mLogger;
    @Mock private EventLogger mEventLogger;

    private FakeClock mNow;
    private Timer mTimer;

    @Before
    public void setUp() throws Exception {
        mNow = new FakeClock(100*1000);

        TimerFactory factory = new TimerFactory(
                InstanceFactory.create(mNow),
                InstanceFactory.create(mLogger),
                InstanceFactory.create(mEventLogger));
        mTimer = factory.create(42, "timer");
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
        FakeClock now = new FakeClock(100*1000);
        Timer timer = new Timer(42, "timer", now, mLogger, mEventLogger);

        assertThat(timer.isActive()).isFalse();

        timer.createFunction(Timer.Function.START).accept(1);
        assertThat(timer.isActive()).isFalse();

        // timer active now
        now.setNow(200*1000);
        assertThat(timer.isActive()).isTrue();

        timer.reset();
        assertThat(timer.isActive()).isFalse();
    }

    @Test
    public void testTimerNoRestart() {
        // Validates that "start" does not restart a timer that is already ongoing.

        mTimer.createFunction(Timer.Function.START).accept(1);
        assertThat(mTimer.isActive()).isFalse();

        mNow.setNow((100+41)*1000);
        assertThat(mTimer.isActive()).isFalse();

        // At t+41 s, a new start() should not reset the timer, it should still expire
        // at the original t+42 s.
        mTimer.createFunction(Timer.Function.START).accept(1);

        mNow.setNow((100+42)*1000);
        assertThat(mTimer.isActive()).isTrue();

        // However once the timer has been activated, we can restart it even without calling end.
        mTimer.createFunction(Timer.Function.START).accept(1);
        assertThat(mTimer.isActive()).isFalse();

        mNow.setNow((100+42+41)*1000);
        assertThat(mTimer.isActive()).isFalse();

        mNow.setNow((100+42+42)*1000);
        assertThat(mTimer.isActive()).isTrue();

        mTimer.createFunction(Timer.Function.END).accept(1);
        assertThat(mTimer.isActive()).isFalse();
    }
}
