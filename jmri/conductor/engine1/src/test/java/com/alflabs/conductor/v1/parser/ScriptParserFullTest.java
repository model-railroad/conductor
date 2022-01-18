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

package com.alflabs.conductor.v1.parser;

import com.alflabs.conductor.jmri.IJmriProvider;
import com.alflabs.conductor.jmri.IJmriThrottle;
import com.alflabs.conductor.v1.ScriptContext;
import com.alflabs.conductor.v1.dagger.DaggerIEngine1TestComponent;
import com.alflabs.conductor.v1.dagger.IEngine1TestComponent;
import com.alflabs.conductor.v1.dagger.IScriptComponent;
import com.alflabs.conductor.v1.script.Script;
import com.alflabs.utils.FakeClock;
import com.alflabs.utils.FakeFileOps;
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

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.when;

/**
 * Tests for both {@link ScriptParser2} *and* {@link Script} execution engine
 * using full script files.
 */
public class ScriptParserFullTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    private TestReporter mReporter;
    private IScriptComponent mScriptComponent;

    @Mock IJmriProvider mJmriProvider;
    @Mock IJmriThrottle mJmriThrottle;

    @Inject FakeClock mClock;
    @Inject FakeFileOps mFileOps;
    @Inject ScriptContext mScriptContext;

    @Before
    public void setUp() throws Exception {
        when(mJmriProvider.getThrottle(42)).thenReturn(mJmriThrottle);
        mReporter = new TestReporter();
        File scriptFile = File.createTempFile("conductor_tests", "tmp");
        scriptFile.deleteOnExit();

        IEngine1TestComponent component = DaggerIEngine1TestComponent
                .factory()
                .createTestComponent(mJmriProvider);
        mScriptComponent = component
                .getScriptComponentFactory()
                .createComponent(mReporter);

        component.inject(this);
        mScriptContext.setScriptFile(scriptFile);
        mClock.setNow(1000);
    }

    @Test
    public void testScript1() throws Exception {
        String source = getFileSource("script1.txt");
        assertThat(source).isNotNull();
        Script script = mScriptComponent.getScriptParser2().parse(source);
        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();
    }

    @Test
    public void testScript2() throws Exception {
        String source = getFileSource("script2.txt");
        assertThat(source).isNotNull();
        Script script = mScriptComponent.getScriptParser2().parse(source);
        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();
    }

    @Test
    public void testScript4() throws Exception {
        String source = getFileSource("script4.txt");
        assertThat(source).isNotNull();
        Script script = mScriptComponent.getScriptParser2().parse(source);
        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();
    }

    @Test
    public void testScript6() throws Exception {
        mFileOps.writeBytes("svg1".getBytes(UTF_8), new File("maps/filename1.svg"));
        mFileOps.writeBytes("svg2".getBytes(UTF_8), new File("maps/filename2.svg"));

        String source = getFileSource("script6.txt");
        assertThat(source).isNotNull();
        Script script = mScriptComponent.getScriptParser2().parse(source);
        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();
    }

    @Test
    public void testScript7() throws Exception {
        mFileOps.writeBytes("GA-ID".getBytes(UTF_8), new File("~/bin/JMRI/rtac_ga_tracking_id.txt"));

        String source = getFileSource("script7.txt");
        assertThat(source).isNotNull();
        Script script = mScriptComponent.getScriptParser2().parse(source);
        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();
    }

    @Test
    public void testScript8() throws Exception {
        mFileOps.writeBytes("GA-ID".getBytes(UTF_8), new File("~/bin/JMRI/rtac_ga_tracking_id.txt"));

        String source = getFileSource("script8.txt");
        assertThat(source).isNotNull();
        Script script = mScriptComponent.getScriptParser2().parse(source);
        assertThat(mReporter.toString()).isEqualTo("");
        assertThat(script).isNotNull();
    }

    private String getFileSource(String fileName) throws IOException {
        String path = new File("v2", fileName).getPath();
        return Resources.toString(Resources.getResource(path), Charsets.UTF_8);
    }
}
