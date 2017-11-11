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

package com.alflabs.conductor.parser;

public class TestReporter extends Reporter {
    private String mReport = "";

    public TestReporter() {
        super(null);
    }

    @Override
    public void log(String msg) {
        if (!mReport.isEmpty() && !mReport.endsWith("\n")) {
            mReport += "\n";
        }
        mReport += msg;
    }

    @Override
    public String toString() {
        return mReport;
    }
}
