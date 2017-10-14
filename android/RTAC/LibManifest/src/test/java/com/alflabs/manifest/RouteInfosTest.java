package com.alflabs.manifest;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class RouteInfosTest {
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetSet() throws Exception {
        RouteInfo r1 = new RouteInfo("Route 1", "S/PA-Toggle", "V/PA-State", "D/204");
        RouteInfo r2 = new RouteInfo("Route 2", "S/BL-Toggle", "V/BL-State", "D/10");
        RouteInfos rs = new RouteInfos(new RouteInfo[] { r1, r2 });

        RouteInfo e1 = new RouteInfo("Route 1", "S/PA-Toggle", "V/PA-State", "D/204");
        RouteInfo e2 = new RouteInfo("Route 2", "S/BL-Toggle", "V/BL-State", "D/10");
        RouteInfos es = new RouteInfos(new RouteInfo[] { e1, e2 });

        assertThat(rs.getRouteInfos()).isEqualTo(new RouteInfo[] { r1, r2 });
        assertThat(rs.getRouteInfos()).isEqualTo(new RouteInfo[] { e1, e2 });

        assertThat(rs.equals(es)).isTrue();
        assertThat(rs.hashCode()).isEqualTo(rs.hashCode());
    }

    @Test
    public void testToJson() throws Exception {
        RouteInfo r1 = new RouteInfo("Route 1", "S/PA-Toggle", "V/PA-State", "D/204");
        RouteInfo r2 = new RouteInfo("Route 2", "S/BL-Toggle", "V/BL-State", "D/10");
        RouteInfos rs = new RouteInfos(new RouteInfo[] { r1, r2 });
        assertThat(rs.toJsonString()).isEqualTo("{\"routeInfos\":[{\"name\":\"Route 1\",\"toggleKey\":\"S/PA-Toggle\",\"statusKey\":\"V/PA-State\",\"throttleKey\":\"D/204\"},{\"name\":\"Route 2\",\"toggleKey\":\"S/BL-Toggle\",\"statusKey\":\"V/BL-State\",\"throttleKey\":\"D/10\"}]}");
    }

    @Test
    public void testFromJson() throws Exception {
        RouteInfos rs = RouteInfos.parseJson("{\"routeInfos\":[{\"name\":\"Route 1\",\"toggleKey\":\"S/PA-Toggle\",\"statusKey\":\"V/PA-State\",\"throttleKey\":\"D/204\"},{\"name\":\"Route 2\",\"toggleKey\":\"S/BL-Toggle\",\"statusKey\":\"V/BL-State\",\"throttleKey\":\"D/10\"}]}");

        RouteInfo e1 = new RouteInfo("Route 1", "S/PA-Toggle", "V/PA-State", "D/204");
        RouteInfo e2 = new RouteInfo("Route 2", "S/BL-Toggle", "V/BL-State", "D/10");
        RouteInfos es = new RouteInfos(new RouteInfo[] { e1, e2 });
        assertThat(rs).isEqualTo(es);
    }

    @Test
    public void testFromEmptyJson() throws Exception {
        RouteInfos r1 = RouteInfos.parseJson(null);
        assertThat(r1).isEqualTo(new RouteInfos(new RouteInfo[0]));

        RouteInfos r2 = RouteInfos.parseJson("");
        assertThat(r2).isEqualTo(new RouteInfos(new RouteInfo[0]));
    }
}
