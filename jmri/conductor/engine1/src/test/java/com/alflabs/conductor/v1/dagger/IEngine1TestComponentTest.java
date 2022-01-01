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

package com.alflabs.conductor.v1.dagger;

import com.alflabs.conductor.jmri.FakeJmriProvider;
import com.alflabs.conductor.jmri.IJmriProvider;
import com.alflabs.conductor.v1.parser.TestReporter;
import com.alflabs.kv.IKeyValue;
import com.alflabs.kv.KeyValueServer;
import com.alflabs.utils.FakeClock;
import com.alflabs.utils.IClock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.inject.Inject;
import java.io.File;

import static com.google.common.truth.Truth.assertThat;

public class IEngine1TestComponentTest {
    @Rule public MockitoRule mRule = MockitoJUnit.rule();


    @Inject IKeyValue mKeyValue1;
    @Inject IKeyValue mKeyValue2;
    @Inject IClock mClock;
    @Inject FakeClock mFakeClock;

    private TestReporter mReporter;
    private IJmriProvider mJmriProvider;
    private IEngine1TestComponent mComponent;

    @Before
    public void setUp() throws Exception {
        mJmriProvider = new FakeJmriProvider();
        mReporter = new TestReporter();
        File scriptFile = File.createTempFile("conductor_tests", "tmp");
        scriptFile.deleteOnExit();

        mComponent = DaggerIEngine1TestComponent
                .factory()
                .createTestComponent(mJmriProvider, scriptFile);
        mComponent.inject(this);
    }

    @Test
    public void testCreateScriptComponent_IsNotSingleton() {
        IScriptComponent scriptComponent1 = mComponent
                .newScriptComponent()
                .createComponent(mReporter);
        IScriptComponent scriptComponent2 = mComponent
                .newScriptComponent()
                .createComponent(mReporter);
        assertThat(scriptComponent1).isNotNull();
        assertThat(scriptComponent2).isNotNull();
        assertThat(scriptComponent1).isNotSameAs(scriptComponent2);
    }

    @Test
    public void testKeyValueServer_IsSingleton() {
        assertThat(mKeyValue1).isNotNull();
        assertThat(mKeyValue1).isSameAs(mKeyValue2);
    }

    @Test
    public void testClockProvider_IsSingleton() {
        assertThat(mClock).isNotNull();
        assertThat(mClock).isSameAs(mFakeClock);
    }
}
