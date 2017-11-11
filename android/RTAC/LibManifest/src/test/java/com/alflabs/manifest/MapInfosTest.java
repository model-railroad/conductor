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

public class MapInfosTest {
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetSet() throws Exception {
        MapInfo m1 = new MapInfo("Map 1", "svg content 1");
        MapInfo m2 = new MapInfo("Map 2", "svg content 2");
        MapInfos ms = new MapInfos(new MapInfo[] { m1, m2 });

        MapInfo e1 = new MapInfo("Map 1", "svg content 1");
        MapInfo e2 = new MapInfo("Map 2", "svg content 2");
        MapInfos es = new MapInfos(new MapInfo[] { e1, e2 });

        assertThat(ms.getMapInfos()).isEqualTo(new MapInfo[] { m1, m2 });
        assertThat(ms.getMapInfos()).isEqualTo(new MapInfo[] { e1, e2 });

        assertThat(ms.equals(es)).isTrue();
        assertThat(ms.hashCode()).isEqualTo(ms.hashCode());
    }

    @Test
    public void testToJson() throws Exception {
        MapInfo m1 = new MapInfo("Map 1", "svg content 1");
        MapInfo m2 = new MapInfo("Map 2", "svg content 2");
        MapInfos ms = new MapInfos(new MapInfo[] { m1, m2 });
        assertThat(ms.toJsonString()).isEqualTo("{\"mapInfos\":[{\"name\":\"Map 1\",\"svg\":\"svg content 1\"},{\"name\":\"Map 2\",\"svg\":\"svg content 2\"}]}");
    }

    @Test
    public void testFromJson() throws Exception {
        MapInfos rs = MapInfos.parseJson("{\"mapInfos\":[{\"name\":\"Map 1\",\"svg\":\"svg content 1\"},{\"name\":\"Map 2\",\"svg\":\"svg content 2\"}]}");

        MapInfo e1 = new MapInfo("Map 1", "svg content 1");
        MapInfo e2 = new MapInfo("Map 2", "svg content 2");
        MapInfos es = new MapInfos(new MapInfo[] { e1, e2 });
        assertThat(rs).isEqualTo(es);
    }

    @Test
    public void testFromEmptyJson() throws Exception {
        MapInfos m1 = MapInfos.parseJson(null);
        assertThat(m1).isEqualTo(new MapInfos(new MapInfo[0]));

        MapInfos m2 = MapInfos.parseJson("");
        assertThat(m2).isEqualTo(new MapInfos(new MapInfo[0]));
    }
}
