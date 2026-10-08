/*
 * Project: Conductor
 * Copyright (C) 2026 alf.labs gmail com,
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

package com.alflabs.conductor.util;

import com.alflabs.conductor.dagger.FakeProcessBuilderAsync;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;

public class ProcessBuilderAsyncTest {

    private FakeProcessBuilderAsync mFakeProcessBuilderAsync;

    @Before
    public void setUp() throws Exception {
        mFakeProcessBuilderAsync = new FakeProcessBuilderAsync();
    }

    @Test
    public void testMockProcessBuilderFactory() throws Exception {
        assertThat(mFakeProcessBuilderAsync.lastInvocation).isNull();

        mFakeProcessBuilderAsync.execAsync(
                Arrays.asList("/tmp/bash", "/tmp/test/command", "arg1"));
        assertThat(mFakeProcessBuilderAsync.lastInvocation)
                .containsExactly("/tmp/bash", "/tmp/test/command", "arg1")
                .inOrder();

        mFakeProcessBuilderAsync.execAsync(
                Arrays.asList("/tmp/bash", "/tmp/test/command", "arg2"));
        assertThat(mFakeProcessBuilderAsync.lastInvocation)
                .containsExactly("/tmp/bash", "/tmp/test/command", "arg2")
                .inOrder();
    }
}
