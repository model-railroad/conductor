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
