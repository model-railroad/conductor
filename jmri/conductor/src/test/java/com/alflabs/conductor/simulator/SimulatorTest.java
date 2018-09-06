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

package com.alflabs.conductor.simulator;

import com.alflabs.conductor.ConductorModule;
import com.alflabs.conductor.DaggerIConductorComponent;
import com.alflabs.conductor.IConductorComponent;
import com.alflabs.conductor.IJmriProvider;
import com.alflabs.conductor.IJmriSensor;
import com.alflabs.conductor.IJmriThrottle;
import com.alflabs.conductor.parser.TestReporter;
import com.alflabs.conductor.script.ExecEngine;
import com.alflabs.conductor.script.IScriptComponent;
import com.alflabs.conductor.script.Script;
import com.alflabs.conductor.script.ScriptModule;
import com.alflabs.rx.Schedulers;
import com.alflabs.rx.Streams;
import com.alflabs.utils.FakeClock;
import com.alflabs.kv.IKeyValue;
import com.alflabs.utils.FileOps;
import com.alflabs.utils.IClock;
import com.alflabs.utils.ILogger;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class SimulatorTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    // @Mock ILogger mLogger;
    @Mock IJmriProvider mJmriProvider;
    @Mock IKeyValue mKeyValue;
    @Mock FileOps mFileOps;
    @Mock IClock mClock;

    private FakeClock mFakeClock;
    private TestReporter mReporter;
    private IScriptComponent mScriptComponent;

    private Simulator mSimulator;
    private ILogger mLogger = new ILogger() {
        @Override
        public void d(String tag, String message) {
            System.out.println(tag + ": " + message);
        }

        @Override
        public void d(String tag, String message, Throwable tr) {
            d(tag, message + " " + tr);
        }
    };

    @Before
    public void setUp() throws Exception {
        mReporter = new TestReporter();

        File file = File.createTempFile("conductor_tests", "tmp");
        file.deleteOnExit();

        when(mKeyValue.getChangedStream()).thenReturn(Streams.<String>stream().on(Schedulers.sync()));

        mFakeClock = new FakeClock(1000);

        IConductorComponent fakeNowComponent = DaggerIConductorComponent.builder()
                .conductorModule(new ConductorModule(mJmriProvider) {
                    @Override
                    public IClock provideClock() {
                        return mFakeClock;
                    }

                    @Override
                    public FileOps provideFileOps() {
                        return mFileOps;
                    }
                })
                .scriptFile(file)
                .build();

        mScriptComponent = fakeNowComponent.newScriptComponent(new ScriptModule(mReporter, mKeyValue));
        mSimulator = new Simulator(mLogger, mClock);
    }

    @Test
    public void testThrottleStop() throws Exception {
        String source = "" +
                "Throttle T1 = 42\n" +
                "String simu = '''\n" +
                "Throttle T1\n" +
                "Stop\n" +
                "'''";

        IJmriThrottle jmriThrottle = mock(IJmriThrottle.class);
        when(mJmriProvider.getThrotlle(42)).thenReturn(jmriThrottle);

        Script script = mScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.createScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        engine.onExecStart();
        verify(mJmriProvider).getThrotlle(42);

        mSimulator.startAsync(script, "simu");
        mSimulator.join();

        verify(jmriThrottle).setSpeed(0);
        assertThat(script.getThrottle("t1").getSpeed()).isEqualTo(0);
    }

    @Test
    public void testSetSensor() throws Exception {
        String source = "" +
                "Sensor S1 = NS42\n" +
                "Sensor S2 = NS43\n" +
                "String simu = '''\n" +
                "Set On S1 ; Set Off S2 ;\n" +
                "'''";

        IJmriSensor jmriSensor1 = mock(IJmriSensor.class);
        IJmriSensor jmriSensor2 = mock(IJmriSensor.class);
        when(mJmriProvider.getSensor("NS42")).thenReturn(jmriSensor1);
        when(mJmriProvider.getSensor("NS43")).thenReturn(jmriSensor2);

        Script script = mScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.createScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        engine.onExecStart();
        verify(mJmriProvider).getSensor("NS42");
        verify(mJmriProvider).getSensor("NS43");

        mSimulator.startAsync(script, "simu");
        mSimulator.join();

        verify(jmriSensor1).setActive(true);
        verify(jmriSensor2).setActive(false);
    }

    @Test
    public void testWaitSensor() throws Exception {
        String source = "" +
                "Sensor S1 = NS42\n" +
                "String simu = '''Wait On S1 ; End'''";

        IJmriSensor jmriSensor1 = mock(IJmriSensor.class);
        when(mJmriProvider.getSensor("NS42")).thenReturn(jmriSensor1);

        Script script = mScriptComponent.createScriptParser2().parse(source);
        ExecEngine engine = mScriptComponent.createScriptExecEngine();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        engine.onExecStart();
        verify(mJmriProvider).getSensor("NS42");

        // The async script calls mClock.sleep() and we wait for that signal.
        when(jmriSensor1.isActive()).thenReturn(false);
        AtomicInteger sleepCount = new AtomicInteger(0);
        doAnswer(invocation -> {
            sleepCount.incrementAndGet();
            when(jmriSensor1.isActive()).thenReturn(true);
            return null;
        }).when(mClock).sleepWithInterrupt(250);

        mSimulator.startAsync(script, "simu");
        mSimulator.join();

        assertThat(sleepCount.get()).isEqualTo(1);
        verify(jmriSensor1, atLeastOnce()).isActive();
        verify(mClock).sleepWithInterrupt(250);
        verifyNoMoreInteractions(mClock);
    }

    @Test
    public void testWaitTimer() throws Exception {
        String source = "" +
                "String simu = '''Wait 5.5s ; End'''";

        Script script = mScriptComponent.createScriptParser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        // The async script calls mClock.sleep() and we wait for that signal.
        AtomicLong elapsedTime = new AtomicLong(1000);
        doAnswer(invocation -> elapsedTime.get()).when(mClock).elapsedRealtime();

        doAnswer(invocation -> {
            elapsedTime.addAndGet(500L);
            return null;
        }).when(mClock).sleepWithInterrupt(500);

        mSimulator.startAsync(script, "simu");
        mSimulator.join();

        assertThat(elapsedTime.get()).isEqualTo(1000 + 5500);
        verify(mClock, times(11)).sleepWithInterrupt(500);
        verify(mClock, times(12)).elapsedRealtime();
        verifyNoMoreInteractions(mClock);
    }

    @Test
    public void testSimul1() throws Exception {
        String source = getFileSource("simul1.txt");
        assertThat(source).isNotNull();
        Script script = mScriptComponent.createScriptParser2().parse(source);
        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriSensor jmriSensor = mock(IJmriSensor.class);
        when(jmriSensor.isActive()).thenReturn(true);
        when(mJmriProvider.getSensor(isA(String.class))).thenReturn(jmriSensor);

        IJmriThrottle jmriThrottle = mock(IJmriThrottle.class);
        when(mJmriProvider.getThrotlle(anyInt())).thenReturn(jmriThrottle);

        // TODO
        // mSimulator.startAsync(script, "Simulator-1");
        // mSimulator.join();
    }

    private String getFileSource(String fileName) throws IOException {
        String path = new File("v2", fileName).getPath();
        return Resources.toString(Resources.getResource(path), Charsets.UTF_8);
    }
}
