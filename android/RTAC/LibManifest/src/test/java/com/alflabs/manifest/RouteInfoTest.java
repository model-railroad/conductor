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
