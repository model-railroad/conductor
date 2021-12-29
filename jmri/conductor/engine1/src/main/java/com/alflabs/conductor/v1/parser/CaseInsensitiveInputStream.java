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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.IntStream;

/**
 * Source: https://gist.github.com/sharwell/9424666
 * License: The "BSD license"
 * Copyright (c) 2012 Terence Parr
 * Copyright (c) 2012 Sam Harwell
 */
public class CaseInsensitiveInputStream extends ANTLRInputStream {
    private char[] mLookaheadData;

    /**
     * Sharwell commented on Mar 7, 2014:
     * For truly case-insensitive languages, a case-insensitive lookahead stream causes the ANTLR
     * lexer to behave as though the input contained only lowercase letters, but messaging and
     * other getText() methods return data from the original input string.
     *
     * This implementation duplicates the data buffer in an effort to improve performance
     * (since calling Character.toLowerCase(char) is quite slow and LA(int) is a
     * performance-critical method). If memory overhead is more important than speed,
     * the mLookaheadData buffer could be removed.
     */
    public CaseInsensitiveInputStream(String input) {
        super(input);
        mLookaheadData = input.toLowerCase().toCharArray();
    }

    @Override
    public int LA(int i) {
        if (i == 0) {
            return 0; // undefined
        }
        if (i < 0) {
            i++; // e.g., translate LA(-1) to use offset i=0; then data[p+0-1]
            if ((p + i - 1) < 0) {
                return IntStream.EOF; // invalid; no char before first char
            }
        }

        if ((p + i - 1) >= n) {
            return IntStream.EOF;
        }

        return mLookaheadData[p + i - 1];
    }
}
