package com.alflabs.manifest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Information on one Map served by Conductor.
 * <p/>
 * This is converted to JSON indirectly via {@link MapInfos}.
 */
public class MapInfo {
    private String name;     // field name exported to JSON
    private String svg;      // field name exported to JSON

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

    @Override
    public String toString() {
        return "MapInfo{" +
                "name='" + name + '\'' +
                ", svg='" + svg + '\'' +
                '}';
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
