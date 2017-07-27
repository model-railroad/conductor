package com.alflabs.manifest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class RouteInfo {
    public String name;         // field exported to JSON
    public String toggleKey;     // field exported to JSON
    public String statusKey;     // field exported to JSON
    public String throttleKey;   // field exported to JSON

    /** Constructor needed by the JSON ObjectMapper. */
    protected RouteInfo() {}

    /**
     * Static description for a route.
     * <p/>
     * The route info uses <em>keys</em>, e.g. the names exported in the KV Server with
     * their prefixes. This are not just the raw IDs using in the script. E.g. a sensor
     * id "B330" would be exported as key "S:B330".
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

    public static RouteInfo parseJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, RouteInfo.class);
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
