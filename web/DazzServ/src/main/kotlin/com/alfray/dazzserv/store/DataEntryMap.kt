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

import com.alflabs.dazzserv.store.DataEntry
import java.util.Collections
import java.util.TreeMap

/// A map of all the entries for a given key.
/// The entries are sorted by ISO timestamp (as strings) in reverse order (most recent first).
/// There can (obviously) be only one entry per timestamp.
data class DataEntryMap(
    val entries: TreeMap<String, DataEntry> =
        TreeMap<String, DataEntry>(Collections.reverseOrder())
) {
    /// Adds an entry if it's new (e.g. a timestamp never seen before).
    /// Already seen timestamps are ignored and not updated.
    /// Returns true if the entry was new and added, false if already seen.
    fun add(entry: DataEntry): Boolean {
        val ts = entry.isoTimestamp
        if (entries.containsKey(ts)) {
            return false
        }
        entries[ts] = entry
        return true
    }
}
