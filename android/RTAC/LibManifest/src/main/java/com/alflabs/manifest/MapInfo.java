package com.alflabs.manifest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MapInfo {
    public final String name;
    public final String svg;

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

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
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
