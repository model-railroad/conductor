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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Information on one Route served by Conductor.
 * <p/>
 * This is converted to JSON indirectly via {@link RouteInfos}.
 */
public class RouteInfo {
    private String name;          // field name exported to JSON
    private String toggleKey;     // field name exported to JSON
    private String statusKey;     // field name exported to JSON
    private String throttleKey;   // field name exported to JSON

    /** Constructor needed by the JSON ObjectMapper. */
    protected RouteInfo() {}

    /**
     * Static description for a route.
     * <p/>
     * The route info uses <em>keys</em>, e.g. the names exported in the KV Server with
     * their prefixes. This are not just the raw IDs using in the script. E.g. a sensor
     * id "B330" would be exported as key "S/B330".
     *
     * @param name The name of the route.
     * @param toggleKey The KV Server key name of the toggle sensor for the route.
     * @param statusKey The KV Server key name of the status enum for the route.
     * @param throttleKey The KV Server key name of the DCC address for the route's speed.
     */
    public RouteInfo(String name, String toggleKey, String statusKey, String throttleKey) {
        this.name = name;
        this.toggleKey = toggleKey;
        this.statusKey = statusKey;
        this.throttleKey = throttleKey;
    }

    public String getName() {
        return name;
    }

    public String getToggleKey() {
        return toggleKey;
    }

    public String getStatusKey() {
        return statusKey;
    }

    public String getThrottleKey() {
        return throttleKey;
    }

    @Override
    public String toString() {
        return "RouteInfo{" +
                "name='" + name + '\'' +
                ", toggleKey='" + toggleKey + '\'' +
                ", statusKey='" + statusKey + '\'' +
                ", throttleKey='" + throttleKey + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RouteInfo)) return false;

        RouteInfo routeInfo = (RouteInfo) o;

        if (!name.equals(routeInfo.name)) return false;
        if (!toggleKey.equals(routeInfo.toggleKey)) return false;
        if (!statusKey.equals(routeInfo.statusKey)) return false;
        return throttleKey.equals(routeInfo.throttleKey);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + toggleKey.hashCode();
        result = 31 * result + statusKey.hashCode();
        result = 31 * result + throttleKey.hashCode();
        return result;
    }
}
