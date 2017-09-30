package com.alflabs.manifest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;

/**
 * A list of {@link RouteInfo} exported or imported to JSON.
 */
public class RouteInfos {
    private RouteInfo[] routeInfos;  // field name exported to JSON

    /** Constructor needed by the JSON ObjectMapper. */
    protected RouteInfos() {}

    /**
     * Descriptions of many Routes.
     *
     * @param routeInfos A non-null possibly empty array of {@link RouteInfo}.
     */
    public RouteInfos(RouteInfo[] routeInfos) {
        this.routeInfos = routeInfos;
    }

    public RouteInfo[] getRouteInfos() {
        return routeInfos;
    }

    public static RouteInfos parseJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, RouteInfos.class);
    }

    public String toJsonString() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    @Override
    public String toString() {
        try {
            return toJsonString();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RouteInfos that = (RouteInfos) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(routeInfos, that.routeInfos);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(routeInfos);
    }
}
