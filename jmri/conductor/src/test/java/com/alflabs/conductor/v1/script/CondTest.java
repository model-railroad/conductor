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

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CondTest {
    @Test
    public void testCond() throws Exception {
        CondCache cc = new CondCache();
        IConditional conditional = mock(IConditional.class);
        Cond cond1 = new Cond(conditional, false /*negated*/);
        Cond cond2 = new Cond(conditional, true /*negated*/);

        when(conditional.isActive()).thenReturn(true);

        assertThat(cond1.eval(cc)).isTrue();
        assertThat(cond2.eval(cc)).isFalse();

        when(conditional.isActive()).thenReturn(false);
        // still returning the same value from the cache
        assertThat(cond1.eval(cc)).isTrue();
        assertThat(cond2.eval(cc)).isFalse();

        cc.clear();
        assertThat(cond1.eval(cc)).isFalse();
        assertThat(cond2.eval(cc)).isTrue();
    }
}
