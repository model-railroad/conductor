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

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.conductor.util.Pair;
import com.alflabs.manifest.MapInfo;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public interface IEngineAdapter {

    /** Sets the script file to load or reload. */
    void setScriptFile(@Null File scriptFile);

    /** Get the script file to load or reload. */
    Optional<File> getScriptFile();

    void onHandle(AtomicBoolean paused);

    Pair<Boolean, File> onReload() throws Exception;

    Optional<MapInfo> getLoadedMapName();

    void appendToLog(StringBuilder status);

    @NonNull
    List<IThrottleDisplayAdapter> getThrottles();

    @NonNull
    List<ISensorDisplayAdapter> getSensors();

    @NonNull
    List<IActivableDisplayAdapter> getBlocks();

    @NonNull
    List<IActivableDisplayAdapter> getTurnouts();
}
