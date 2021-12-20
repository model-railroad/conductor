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

import com.alflabs.annotations.NonNull;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;

/**
 * A list of {@link MapInfo} exported or imported to JSON.
 */
public class MapInfos {
    private MapInfo[] mapInfos;  // field name exported to JSON

    /** Constructor needed by the JSON ObjectMapper. */
    protected MapInfos() {}

    /**
     * Descriptions of many Maps.
     *
     * @param mapInfos A non-null possibly empty array of {@link MapInfo}.
     */
    public MapInfos(@NonNull MapInfo[] mapInfos) {
        this.mapInfos = mapInfos;
    }

    @NonNull
    public MapInfo[] getMapInfos() {
        return mapInfos;
    }

    @NonNull
    public static MapInfos parseJson(String json) throws IOException {
        if (json == null || json.isEmpty()) {
            return new MapInfos(new MapInfo[0]);
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, MapInfos.class);
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

        MapInfos that = (MapInfos) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(mapInfos, that.mapInfos);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(mapInfos);
    }
}
