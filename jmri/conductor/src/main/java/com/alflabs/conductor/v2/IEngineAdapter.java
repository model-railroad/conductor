/*
 * Project: Conductor
 * Copyright (C) 2022 alf.labs gmail com,
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

package com.alflabs.conductor.v2;

import com.alflabs.annotations.Null;
import com.alflabs.conductor.util.Pair;
import com.alflabs.manifest.MapInfo;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public interface IEngineAdapter {
    Optional<File> getScriptFile();

    void setScriptFile(@Null File scriptFile);

    void onHandle(AtomicBoolean paused);

    Pair<Boolean, File> onReload() throws Exception;

    Optional<MapInfo> getLoadedMapName();

    void appendToLog(StringBuilder status);
}
