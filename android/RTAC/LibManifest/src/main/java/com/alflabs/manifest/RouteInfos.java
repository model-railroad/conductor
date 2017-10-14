package com.alflabs.manifest;

import com.alflabs.annotations.NonNull;
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
    public RouteInfos(@NonNull RouteInfo[] routeInfos) {
        this.routeInfos = routeInfos;
    }

    @NonNull
    public RouteInfo[] getRouteInfos() {
        return routeInfos;
    }

    @NonNull
    public static RouteInfos parseJson(String json) throws IOException {
        if (json == null || json.isEmpty()) {
            return new RouteInfos(new RouteInfo[0]);
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, RouteInfos.class);
    }

    @NonNull
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
