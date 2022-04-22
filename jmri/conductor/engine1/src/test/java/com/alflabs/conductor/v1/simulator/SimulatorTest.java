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

package com.alflabs.conductor.v1.simulator;

import com.alflabs.conductor.jmri.IJmriProvider;
import com.alflabs.conductor.jmri.IJmriSensor;
import com.alflabs.conductor.jmri.IJmriThrottle;
import com.alflabs.conductor.v1.Script1Context;
import com.alflabs.conductor.v1.dagger.DaggerIEngine1TestComponent;
import com.alflabs.conductor.v1.dagger.IEngine1TestComponent;
import com.alflabs.conductor.v1.dagger.IScript1Component;
import com.alflabs.conductor.v1.parser.TestReporter;
import com.alflabs.conductor.v1.script.ExecEngine1;
import com.alflabs.conductor.v1.script.Script1;
import com.alflabs.utils.FakeClock;
import com.alflabs.utils.ILogger;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SimulatorTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock IJmriProvider mJmriProvider;

    @Inject ILogger mLogger;
    @Inject FakeClock mClock;
    @Inject
    Script1Context mScriptContext;

    private TestReporter mReporter;
    private IScript1Component mScriptComponent;
    private Simulator mSimulator;

    @Before
    public void setUp() throws Exception {
        mReporter = new TestReporter();
        File scriptFile = File.createTempFile("conductor_tests", "tmp");
        scriptFile.deleteOnExit();

        IEngine1TestComponent component = DaggerIEngine1TestComponent
                .factory()
                .createTestComponent(mJmriProvider);
        component.inject(this);
        mScriptContext.setScript1File(scriptFile);

        mClock.setNow(1000);

        mScriptComponent = component
                .getScriptComponentFactory()
                .createComponent(mReporter);
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
        when(mJmriProvider.getThrottle(42)).thenReturn(jmriThrottle);

        Script1 script = mScriptComponent.getScript1Parser2().parse(source);
        ExecEngine1 engine = mScriptComponent.getExecEngine1();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        engine.onExecStart();
        verify(mJmriProvider).getThrottle(42);

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

        Script1 script = mScriptComponent.getScript1Parser2().parse(source);
        ExecEngine1 engine = mScriptComponent.getExecEngine1();

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

        Script1 script = mScriptComponent.getScript1Parser2().parse(source);
        ExecEngine1 engine = mScriptComponent.getExecEngine1();

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        engine.onExecStart();
        verify(mJmriProvider).getSensor("NS42");

        // The async script calls mClock.sleep() and we wait for that signal.
        assertThat(mClock.elapsedRealtime()).isEqualTo(1000);
        when(jmriSensor1.isActive()).thenReturn(false);
        AtomicInteger sleepCount = new AtomicInteger(0);
        mClock.setSleepCallback(sleepTimeMs -> {
                    assertThat(sleepTimeMs).isEqualTo(250);
                    sleepCount.incrementAndGet();
                    when(jmriSensor1.isActive()).thenReturn(true);
                });

        mSimulator.startAsync(script, "simu");
        mSimulator.join();
        mClock.setSleepCallback(null);

        assertThat(mClock.elapsedRealtime()).isEqualTo(1250);
        assertThat(sleepCount.get()).isEqualTo(1);
        verify(jmriSensor1, atLeastOnce()).isActive();
    }

    @Test
    public void testWaitTimer() throws Exception {
        String source = "" +
                "String simu = '''Wait 5.5s ; End'''";

        Script1 script = mScriptComponent.getScript1Parser2().parse(source);

        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        // The async script calls mClock.sleep() and we wait for that signal.
        assertThat(mClock.elapsedRealtime()).isEqualTo(1000);

        mSimulator.startAsync(script, "simu");
        mSimulator.join();

        assertThat(mClock.elapsedRealtime()).isEqualTo(1000 + 5500);
    }

    @Test
    public void testSimul1() throws Exception {
        String source = getFileSource("simul1.txt");
        assertThat(source).isNotNull();
        Script1 script = mScriptComponent.getScript1Parser2().parse(source);
        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();

        IJmriSensor jmriSensor = mock(IJmriSensor.class);
        when(jmriSensor.isActive()).thenReturn(true);
        when(mJmriProvider.getSensor(isA(String.class))).thenReturn(jmriSensor);

        IJmriThrottle jmriThrottle = mock(IJmriThrottle.class);
        when(mJmriProvider.getThrottle(anyInt())).thenReturn(jmriThrottle);

        // TODO
        // mSimulator.startAsync(script, "Simulator-1");
        // mSimulator.join();
    }

    private String getFileSource(String fileName) throws IOException {
        String path = new File("v2", fileName).getPath();
        return Resources.toString(Resources.getResource(path), Charsets.UTF_8);
    }
}
