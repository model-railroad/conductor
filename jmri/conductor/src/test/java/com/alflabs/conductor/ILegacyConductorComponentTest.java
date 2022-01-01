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

package com.alflabs.conductor;

import com.alflabs.conductor.dagger.LegacyCommonModule;
import com.alflabs.conductor.jmri.IJmriProvider;
import com.alflabs.kv.KeyValueServer;
import com.alflabs.utils.IClock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;

import static com.google.common.truth.Truth.assertThat;

@Deprecated /* covered by engine1 IEngine1TestComponentTest. */
public class ILegacyConductorComponentTest {
    @Rule public MockitoRule mRule = MockitoJUnit.rule();

    @Mock IJmriProvider mJmriProvider;

    private ILegacyConductorComponent mComponent;

    @Before
    public void setUp() throws Exception {
        File file = File.createTempFile("conductor_tests", "tmp");
        file.deleteOnExit();

        mComponent = DaggerILegacyConductorComponent
                .builder()
                .legacyCommonModule(new LegacyCommonModule(mJmriProvider))
                .scriptFile(file)
                .build();
    }

    @Test
    public void testKeyValueServerIsSingleton() throws Exception {
        KeyValueServer kv1 = mComponent.getKeyValueServer();
        KeyValueServer kv2 = mComponent.getKeyValueServer();
        assertThat(kv1).isNotNull();
        assertThat(kv1).isSameAs(kv2);
    }

    @Test
    public void testNowProviderIsSingleton() throws Exception {
        IClock np1 = mComponent.getClock();
        IClock np2 = mComponent.getClock();
        assertThat(np1).isNotNull();
        assertThat(np1).isSameAs(np2);
    }
}
