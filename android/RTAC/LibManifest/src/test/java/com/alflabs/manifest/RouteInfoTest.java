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
        RouteInfo r1 = new RouteInfo("Route Name", "S:PA-Toggle", "V:PA-State", "D:204");
        assertThat(r1.getName()).isEqualTo("Route Name");
        assertThat(r1.getToggleKey()).isEqualTo("S:PA-Toggle");
        assertThat(r1.getStatusKey()).isEqualTo("V:PA-State");
        assertThat(r1.getThrottleKey()).isEqualTo("D:204");

        RouteInfo r2 = new RouteInfo("Route Name", "S:PA-Toggle", "V:PA-State", "D:204");
        assertThat(r1.equals(r2)).isTrue();
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    public void testToJson() throws Exception {
        RouteInfo r = new RouteInfo("Route Name", "S:PA-Toggle", "V:PA-State", "D:204");
        assertThat(r.toJsonString()).isEqualTo("{" +
                "\"name\":\"Route Name\"," +
                "\"toggleKey\":\"S:PA-Toggle\"," +
                "\"statusKey\":\"V:PA-State\"," +
                "\"throttleKey\":\"D:204\"}");
    }

    @Test
    public void testFromJson() throws Exception {
        RouteInfo r = RouteInfo.parseJson("{" +
                "\"name\":\"Route Name\"," +
                "\"toggleKey\":\"S:PA-Toggle\"," +
                "\"statusKey\":\"V:PA-State\"," +
                "\"throttleKey\":\"D:204\"}");
        assertThat(r).isEqualTo(new RouteInfo("Route Name", "S:PA-Toggle", "V:PA-State", "D:204"));
    }
}
