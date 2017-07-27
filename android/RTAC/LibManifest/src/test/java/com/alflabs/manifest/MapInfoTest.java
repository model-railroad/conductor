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
        MapInfo m1 = new MapInfo("Map Name", "svg content");
        assertThat(m1.getName()).isEqualTo("Map Name");
        assertThat(m1.getSvg()).isEqualTo("svg content");

        MapInfo m2 = new MapInfo("Map Name", "svg content");
        assertThat(m1.equals(m2)).isTrue();
        assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
    }

    @Test
    public void testToJson() throws Exception {
        MapInfo map = new MapInfo("Map Name", "<svg type=\"content\" attribute='something'/>");
        assertThat(map.toJsonString()).isEqualTo("{\"name\":\"Map Name\",\"svg\":\"<svg type=\\\"content\\\" attribute='something'/>\"}");
    }

    @Test
    public void testFromJson() throws Exception {
        MapInfo map = MapInfo.parseJson("{\"name\":\"Map Name\",\"svg\":\"<svg type=\\\"content\\\" attribute='something'/>\"}");
        assertThat(map).isEqualTo(new MapInfo("Map Name", "<svg type=\"content\" attribute='something'/>"));
    }
}
