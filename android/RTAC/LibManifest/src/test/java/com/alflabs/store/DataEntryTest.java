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

package com.alflabs.store;

import com.alflabs.dazzserv.store.DataEntry;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class DataEntryTest {

    @Test
    public void testEntryToJson() throws Exception {
        DataEntry entry = new DataEntry("toggles/entry1", "1970-01-01T00:03:54Z", true, "( some payload )");
        String json = entry.toJsonString(/*mapper=*/ null);

        assertThat(json).isEqualTo(
                "{\"key\":\"toggles/entry1\",\"ts\":\"1970-01-01T00:03:54Z\",\"st\":true,\"d\":\"( some payload )\"}"
        );
    }

    @Test
    public void testJsonToEntry() throws Exception {
        String json = "{\"key\":\"toggles/entry1\",\"ts\":\"1970-01-01T00:03:54Z\",\"st\":true,\"d\":\"( some payload )\"}";
        DataEntry entry = DataEntry.parseJson(/*mapper=*/ null, json);

        assertThat(entry).isEqualTo(
                new DataEntry("toggles/entry1", "1970-01-01T00:03:54Z", true, "( some payload )")
        );
    }
}
