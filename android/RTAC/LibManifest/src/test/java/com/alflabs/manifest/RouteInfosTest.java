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

public class RouteInfosTest {
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetSet() throws Exception {
        RouteInfo r1 = new RouteInfo("Route 1", "S/PA-Toggle", "V/PA-State", "V/PA-Counter", "D/204");
        RouteInfo r2 = new RouteInfo("Route 2", "S/BL-Toggle", "V/BL-State", "V/BL-Counter", "D/10");
        RouteInfos rs = new RouteInfos(new RouteInfo[] { r1, r2 });

        RouteInfo e1 = new RouteInfo("Route 1", "S/PA-Toggle", "V/PA-State", "V/PA-Counter", "D/204");
        RouteInfo e2 = new RouteInfo("Route 2", "S/BL-Toggle", "V/BL-State", "V/BL-Counter", "D/10");
        RouteInfos es = new RouteInfos(new RouteInfo[] { e1, e2 });

        assertThat(rs.getRouteInfos()).isEqualTo(new RouteInfo[] { r1, r2 });
        assertThat(rs.getRouteInfos()).isEqualTo(new RouteInfo[] { e1, e2 });

        assertThat(rs.equals(es)).isTrue();
        assertThat(rs.hashCode()).isEqualTo(rs.hashCode());
    }

    @Test
    public void testToJson() throws Exception {
        RouteInfo r1 = new RouteInfo("Route 1", "S/PA-Toggle", "V/PA-State", "V/PA-Counter", "D/204");
        RouteInfo r2 = new RouteInfo("Route 2", "S/BL-Toggle", "V/BL-State", "V/BL-Counter", "D/10");
        RouteInfos rs = new RouteInfos(new RouteInfo[] { r1, r2 });
        assertThat(rs.toJsonString()).isEqualTo("{\"routeInfos\":[" +
                "{\"name\":\"Route 1\",\"toggleKey\":\"S/PA-Toggle\",\"statusKey\":\"V/PA-State\",\"counterKey\":\"V/PA-Counter\",\"throttleKey\":\"D/204\"}," +
                "{\"name\":\"Route 2\",\"toggleKey\":\"S/BL-Toggle\",\"statusKey\":\"V/BL-State\",\"counterKey\":\"V/BL-Counter\",\"throttleKey\":\"D/10\"}]}");
    }

    @Test
    public void testFromJson1() throws Exception {
        RouteInfos rs = RouteInfos.parseJson("{\"routeInfos\":[" +
                "{\"name\":\"Route 1\"}," +
                "{\"name\":\"Route 2\"}]}");

        RouteInfo e1 = new RouteInfo("Route 1", null, null, null, null);
        RouteInfo e2 = new RouteInfo("Route 2", null, null, null, null);
        RouteInfos es = new RouteInfos(new RouteInfo[] { e1, e2 });
        assertThat(rs).isEqualTo(es);
    }

    @Test
    public void testFromJson2() throws Exception {
        RouteInfos rs = RouteInfos.parseJson("{\"routeInfos\":[" +
                "{\"name\":\"Route 1\",\"toggleKey\":\"S/PA-Toggle\",\"statusKey\":\"V/PA-State\",\"throttleKey\":\"D/204\"}," +
                "{\"name\":\"Route 2\",\"toggleKey\":\"S/BL-Toggle\",\"statusKey\":\"V/BL-State\",\"throttleKey\":\"D/10\"}]}");

        RouteInfo e1 = new RouteInfo("Route 1", "S/PA-Toggle", "V/PA-State", null, "D/204");
        RouteInfo e2 = new RouteInfo("Route 2", "S/BL-Toggle", "V/BL-State", null, "D/10");
        RouteInfos es = new RouteInfos(new RouteInfo[] { e1, e2 });
        assertThat(rs).isEqualTo(es);
    }

    @Test
    public void testFromJson3() throws Exception {
        RouteInfos rs = RouteInfos.parseJson("{\"routeInfos\":[" +
                "{\"name\":\"Route 1\",\"toggleKey\":\"S/PA-Toggle\",\"statusKey\":\"V/PA-State\",\"counterKey\":\"V/PA-Counter\",\"throttleKey\":\"D/204\"}," +
                "{\"name\":\"Route 2\",\"toggleKey\":\"S/BL-Toggle\",\"statusKey\":\"V/BL-State\",\"counterKey\":\"V/BL-Counter\",\"throttleKey\":\"D/10\"}]}");

        RouteInfo e1 = new RouteInfo("Route 1", "S/PA-Toggle", "V/PA-State", "V/PA-Counter", "D/204");
        RouteInfo e2 = new RouteInfo("Route 2", "S/BL-Toggle", "V/BL-State", "V/BL-Counter", "D/10");
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
