/*
 * Project: LibManifest
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

package com.alflabs.manifest;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class RouteInfoTest {
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetSet() throws Exception {
        RouteInfo r1 = new RouteInfo("Route Name", "S/PA-Toggle", "V/PA-State", "D/204");
        assertThat(r1.getName()).isEqualTo("Route Name");
        assertThat(r1.getToggleKey()).isEqualTo("S/PA-Toggle");
        assertThat(r1.getStatusKey()).isEqualTo("V/PA-State");
        assertThat(r1.getThrottleKey()).isEqualTo("D/204");

        RouteInfo r2 = new RouteInfo("Route Name", "S/PA-Toggle", "V/PA-State", "D/204");
        assertThat(r1.equals(r2)).isTrue();
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }
}
