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

import java.net.URI;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

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
        assertThat(m1.toURI().toString()).isEqualTo("uri/path/svg");


        MapInfo m2 = new MapInfo("Map Name", "svg content", "uri/path/svg");
        assertThat(m1.equals(m2)).isTrue();
        assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        assertThat(m2.toURI().toString()).isEqualTo("uri/path/svg");
    }

    @Test
    public void testWindowsPath() throws Exception {
        MapInfo m1 = new MapInfo("Map Name", "svg content", "\\\\temp\\windows\\path\\my map.svg");
        assertThat(m1.getName()).isEqualTo("Map Name");
        assertThat(m1.getUri()).isEqualTo("\\\\temp\\windows\\path\\my map.svg");
        assertThat(m1.getSvg()).isEqualTo("svg content");

        // This fails because the space is not properly URL encoded:
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            URI ignore = URI.create(m1.getUri());
        });
        assertThat(e.getMessage()).contains("Illegal character in path at index");

        // This is the proper way to get the URI for the map.
        assertThat(m1.toURI().toString()).isEqualTo("//temp/windows/path/my%20map.svg");
    }
}
