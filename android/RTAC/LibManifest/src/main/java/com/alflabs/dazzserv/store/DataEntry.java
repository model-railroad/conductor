/*
 * Project: LibManifest
 * Copyright (C) 2025 alf.labs gmail com,
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

package com.alflabs.dazzserv.store;


import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * One data entry unit: key (category) -> ISO timestamp --> boolean state --> opaque payload.
 */
public class DataEntry {
    /* The key is expected to be a path-like structure (item1/item2/.../itemN) and never empty. */
    private String mKey;

    /*  The timestamp MUST be in ISO 8601 format: .e.g "1970-01-01T00:03:54Z"
        Consistency is important across all entries as string natural sorting is used to
        order the timestamps. This avoids having to decode the ISO timestamp in the store.
     */
    private String mIsoTimestamp;

    /*  The "state" is a boolean which meaning depends on the key and the application.
        Pretty much all the data items handled by Wazz incorporate a boolean state, although its
        meaning depends on the context of the data. It is thus extracted from the payload.
     */
    private boolean mState = false;

    /*  The payload is an opaque string which the DataStore doesn't need to decode. It can be
        empty if needed. Most of the time it will be application-specific stringified JSON.
        Keeping it opaque means we clumsily encode a JSON String into a JSON, but OTOH it means
        the DataStore and the REST server does not need to be updated with the application.
     */
    private String mPayload = "";

    @JsonCreator
    public DataEntry(
            @NonNull @JsonProperty("key") String key,
            @NonNull @JsonProperty("ts" ) String isoTimestamp,
                     @JsonProperty("st" ) boolean state,
            @Null    @JsonProperty("d"  ) String payload
    ) {
        setKey(key);
        setIsoTimestamp(isoTimestamp);
        setState(state);
        setPayload(payload);
    }

    @NonNull
    @JsonProperty("key")
    public String getKey() {
        return mKey;
    }

    public void setKey(@NonNull String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("DataEntry.Key is mandatory");
        }
        this.mKey = key;
    }

    @NonNull
    @JsonProperty("ts")
    public String getIsoTimestamp() {
        return mIsoTimestamp;
    }

    public void setIsoTimestamp(@NonNull String isoTimestamp) {
        if (isoTimestamp == null || isoTimestamp.isEmpty()) {
            throw new IllegalArgumentException("DataEntry.IsoTimestamp is mandatory");
        }
        this.mIsoTimestamp = isoTimestamp;
    }

    @JsonProperty("st")
    public boolean isState() {
        return mState;
    }

    public void setState(boolean state) {
        this.mState = state;
    }

    @NonNull
    @JsonProperty("d")
    public String getPayload() {
        return mPayload;
    }

    public void setPayload(@Null String payload) {
        this.mPayload = payload == null ? "" : payload;
    }

    @NonNull
    public static DataEntry parseJson(
            @Null ObjectMapper mapper,
            @Null String json) throws IOException {
        if (json == null || json.isEmpty()) {
            throw new IllegalArgumentException("Invalid DataEntry JSON (empty or null)");
        }
        if (mapper == null) {
            mapper = new ObjectMapper();
        }
        return mapper.readValue(json, DataEntry.class);
    }

    @NonNull
    public String toJsonString(@Null ObjectMapper mapper) throws JsonProcessingException {
        if (mapper == null) {
            mapper = new ObjectMapper();
        }
        return mapper.writeValueAsString(this);
    }

    @Override
    public String toString() {
        return "DataEntry{" +
                "mKey='" + mKey + '\'' +
                ", mIsoTimestamp='" + mIsoTimestamp + '\'' +
                ", mState=" + mState +
                ", mPayload='" + mPayload + '\'' +
                '}';
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof DataEntry)) return false;

        DataEntry dataEntry = (DataEntry) o;
        return mState == dataEntry.mState &&
                mKey.equals(dataEntry.mKey) &&
                mIsoTimestamp.equals(dataEntry.mIsoTimestamp) && mPayload.equals(dataEntry.mPayload);
    }

    @Override
    public int hashCode() {
        int result = mKey.hashCode();
        result = 31 * result + mIsoTimestamp.hashCode();
        result = 31 * result + Boolean.hashCode(mState);
        result = 31 * result + mPayload.hashCode();
        return result;
    }
}

