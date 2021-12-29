/*
 * Project: Conductor
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

package com.alflabs.conductor.v1.parser;

import java.util.HashMap;
import java.util.Map;

public class LineCounter {
    private final String mSource;
    private final Map<Integer, StartEnd> mOffsets = new HashMap<>();
    /** Last parsed index line (which should thus be in the map, except for index 0).
     * Start at zero. First line is 1. */
    private int mLastParsedIndex;
    /** The offset of the first char after the last parsed index or maxed to Source's length. */
    private int mLastParsedOffset;

    public LineCounter(String source) {
        // Expect the source to be properly formatted with lines ending with \n or \r\n.
        // Files using purely \r can be mapped to purely \n files.
        // However improper mixed formats are not going to be handled properly.
        if (source.indexOf('\n') == -1 && source.indexOf('\r') !=-1) {
            source = source.replace('\r', '\n');
        }
        mSource = source;
    }

    /**
     * Returns the line or an empty string.
     * @param index 1-based line count.
     * @return The line requested (including line separator) or an empty string in case of error.
     */
    public String getLine(int index) {
        StartEnd startEnd = mOffsets.get(index);

        if (startEnd == null) {
            startEnd = findLine(index);
        }

        if (startEnd != null && startEnd.isValid()) {
            return mSource.substring(startEnd.getStart(), startEnd.getEnd());
        }
        return "";
    }

    private StartEnd findLine(int index) {
        int len = mSource.length();
        while (mLastParsedOffset < len && mLastParsedIndex < index) {
            // The following only works properly for \n or \r\n delimited files.
            //StartEnd se = new
            int start = mLastParsedOffset;
            int end = mSource.indexOf('\n', mLastParsedOffset);
            if (end == -1) {
                end = len - 1;
            }
            mLastParsedIndex++;
            mLastParsedOffset = end + 1;
            StartEnd se = new StartEnd(start, mLastParsedOffset);
            mOffsets.put(mLastParsedIndex, se);
            if (mLastParsedIndex == index) {
                return se;
            }
        }

        return null;
    }

    private static class StartEnd {
        private final int mStart;
        private final int mEnd;

        public StartEnd(int start, int end) {
            mStart = start;
            mEnd = end;
        }

        public int getStart() {
            return mStart;
        }

        public int getEnd() {
            return mEnd;
        }

        public boolean isValid() {
            return mEnd > mStart;
        }
    }
}
