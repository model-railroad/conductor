/*
 * Project: DazzServ
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

package com.alfray.dazzserv.store

import com.fasterxml.jackson.annotation.JsonProperty

/// One data entry unit: key (category) -> ISO timestamp --> boolean state --> opaque payload.
data class DataEntry(
    /// The key is expected to be a path-like structure (item1/item2/.../itemN) and never empty.
    val key: String,
    /// The timestamp MUST be in ISO 8601 format: .e.g "1970-01-01T00:03:54Z"
    /// Consistency is important across all entries as string natural sorting is used to
    /// order the timestamps. This avoids having to decode the ISO timestamp in the store.
    @JsonProperty("ts") val isoTimestamp: String,
    /// The "state" is a boolean which meaning depends on the key and the application.
    /// Pretty much all the data items handled by Wazz incorporate a boolean state, although its
    /// meaning depends on the context of the data. It is thus extracted from the payload.
    @JsonProperty("st") val state: Boolean = false,
    /// The payload is an opaque string which the DataStore doesn't need to decode. It can be
    /// empty if needed. Most of the time it will be application-specific stringified JSON.
    /// Keeping it opaque means we clumsily encode a JSON String into a JSON, but OTOH it means
    /// the DataStore and the REST server does not need to be updated with the application.
    @JsonProperty("d")  val payload: String = "",
)
