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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Information on one Map served by Conductor.
 * <p/>
 * This is converted to JSON indirectly via {@link MapInfos}.
 */
public class MapInfo {
    private String name;     // field name exported to JSON
    private String svg;      // field name exported to JSON
    private String uri;      // field name exported to JSON

    /**
     * Creates a MapInfo with the given map name, and map SVG _content_ string.
     *
     * The URI needs to be sanitized to make it compatible with java.net.URI:
     * Window-path separator (\) is automatically converted to a URL-path separator (/),
     * and other characters escaped as needed. This is done by the toURI() method, whilst
     * the getUri() method returns the underlying non-sanitized version.
     */
    @JsonCreator
    public MapInfo(
            @JsonProperty("name") String name,
            @JsonProperty("svg") String svg,
            @JsonProperty("uri") String uri) {
        this.name = name;
        this.svg = svg;
        this.uri = uri == null ? name : uri; // for legacy before uri was present
    }

    public String getName() {
        return name;
    }

    public String getSvg() {
        return svg;
    }

    /** Returns the raw underlying URI. It may not be fully properly encoded.
     * Compare with toURI(). */
    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "MapInfo{" +
                "name='" + name + '\'' +
                ", uri='" + uri + '\'' +
                ", svg='" + svg + '\'' +
                '}';
    }

    public URI toURI() throws URISyntaxException {
        final String scheme = null;
        final String host = null;
        final String path = uri.replaceAll("\\\\", "/");
        final String fragment = null;
        return new URI(scheme, host, path, fragment);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapInfo)) return false;

        MapInfo mapInfo = (MapInfo) o;

        if (!name.equals(mapInfo.name)) return false;
        if (!uri.equals(mapInfo.uri)) return false;
        return svg.equals(mapInfo.svg);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + uri.hashCode();
        result = 31 * result + svg.hashCode();
        return result;
    }
}
