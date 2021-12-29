/*
 * Project: Conductor
 * Copyright (C) 2019 alf.labs gmail com,
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

package com.alflabs.conductor.v1.script;

import com.alflabs.conductor.util.JsonSender;

public class JsonEventAction implements IAction {

    private final JsonSender mJsonSender;
    private final String mKey1;
    private final String mKey2;
    private final String mValue;

    public JsonEventAction(JsonSender jsonSender, String key1, String key2, String value) {
        mJsonSender = jsonSender;
        mKey1 = key1;
        mKey2 = key2;
        mValue = value;
    }

    @Override
    public void execute() {
        mJsonSender.sendEvent(mKey1, mKey2, mValue);
    }
}
