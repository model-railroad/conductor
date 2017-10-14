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
