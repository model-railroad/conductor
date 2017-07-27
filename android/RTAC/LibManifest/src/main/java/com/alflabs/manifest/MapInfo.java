package com.alflabs.manifest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class MapInfo {
    public String name;     // field exported to JSON
    public String svg;      // field exported to JSON

    /** Constructor needed by the JSON ObjectMapper. */
    protected MapInfo() {}

    public MapInfo(String name, String svg) {
        this.name = name;
        this.svg = svg;
    }

    public String getName() {
        return name;
    }

    public String getSvg() {
        return svg;
    }

    public static MapInfo parseJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, MapInfo.class);
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
        if (!(o instanceof MapInfo)) return false;

        MapInfo mapInfo = (MapInfo) o;

        if (!name.equals(mapInfo.name)) return false;
        return svg.equals(mapInfo.svg);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + svg.hashCode();
        return result;
    }
}
