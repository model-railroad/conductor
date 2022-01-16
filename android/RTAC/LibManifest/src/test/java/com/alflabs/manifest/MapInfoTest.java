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

public class MapInfoTest {
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetSet() throws Exception {
        MapInfo m1 = new MapInfo("Map Name", "svg content", "uri/path/svg");
        assertThat(m1.getName()).isEqualTo("Map Name");
        assertThat(m1.getUri()).isEqualTo("uri/path/svg");
        assertThat(m1.getSvg()).isEqualTo("svg content");

        MapInfo m2 = new MapInfo("Map Name", "svg content", "uri/path/svg");
        assertThat(m1.equals(m2)).isTrue();
        assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
    }
}
